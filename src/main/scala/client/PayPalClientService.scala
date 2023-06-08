package client
import org.http4s.client.Client
import io.circe.Json
import org.http4s._
import org.http4s.circe._
import org.http4s.client._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.implicits._
import cats.effect.kernel.Async
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe.CirceEntityDecoder._
import domain._
import domain.OrderData._
import io.circe.syntax._
import cats.syntax.all._
import org.http4s.client.middleware.RequestLogger
import retry.RetryPolicy
import config.PayPalCredentials
import retries._
import org.http4s.Status
import cats.effect.syntax.all._
//import cats.effect.syntax.AllSyntax
import org.typelevel.log4cats.Logger
final case class PayPalClientService[F[_]: Async: Logger]()
    extends Http4sClientDsl[F] {
  def fetchAccessToken(
     client:Client[F],
      endpointUrl: String,// Uri.uri("https://api-m.sandbox.paypal.com")
      credentials:PayPalCredentials
  ): F[AccessTokenResponse] = {

    val request = Method.POST(
      UrlForm(
        "grant_type" -> "client_credentials"
      ),
      Uri.unsafeFromString(endpointUrl + "/v1/oauth2/token"),
      Accept(MediaType.application.json),
      Authorization(
        BasicCredentials(credentials.payPalClientId.value, credentials.payPalClientSecret.value)
      ) // should it be changed to base64??
    )
    client.expect(request)(jsonOf[F, AccessTokenResponse])

  }
// after payment is approved, it is captured
  def capturePayment(
      client: Client[F],
      accessToken: String,
      endpointUrl: String,
      orderId: String,
      intent: Intent
  ): F[String] = {

    val request = Method.POST
      .apply(
        Uri.unsafeFromString(
          endpointUrl + "/v2/checkout/orders/" + orderId + "/capture"
        ),
        Accept(MediaType.application.json),
        Authorization(Credentials.Token(AuthScheme.Bearer, accessToken))
      )
      .withEmptyBody

    client.run(request).use { resp =>
      resp.status match {
        case Status.Ok | Status.Conflict =>
          resp.as[String]
        case st =>
          PaymentError(
            Option(st.reason).getOrElse("unknown")
          ).raiseError[F, String]
      }
    }

  }

  def createOrder(
      client: Client[F],
      accessToken: String,
      endpointUrl: String,
      data: OrderData
  ): F[CreateOrderResponse] = {
    print(data.asJson)
    val request = Method.POST(
      data,
      Uri.unsafeFromString(endpointUrl + "/v2/checkout/orders"),
      Accept(MediaType.application.json),
      Authorization(Credentials.Token(AuthScheme.Bearer, accessToken))
    )
    client.run(request).use { resp =>
      resp.status match {
        //use Http status code 2oo for successful requests that retrieve or update a resource
        //Use Http status code 201 for successful requests that create a new resource on the server
        //use Http status code 202 for requests that have been accepted for processing but 
        ///processing has not yet been completed
        // use Http status code 204 for successful requests that delete a resource or do not have any content to return
        case Status.Ok |Status.Created =>
          // from Byte=>Json we use entityDecoder and from Json =>case class, we use Decoder
          resp.asJsonDecode[CreateOrderResponse]
        case st =>
          
          PaymentError(
            Option(st.toString()).getOrElse("unknown")
          ).raiseError[F, CreateOrderResponse]
      }
    }

  }
  def createOrderWithRetry(
      client: Client[F],
      accessToken: String,
      endpointUrl: String,
      data: OrderData,
      policy: RetryPolicy[F]
  ) =
    Retry[F]
      .retry(policy, Retriable.Orders)(
        createOrder(client, accessToken, endpointUrl, data)
      ) //
      .adaptError { case e =>
        OrderError(Option(e.getMessage).getOrElse("Unknown"))
      }

  def capturePaymentWithRetry(
      client: Client[F],
      accessToken: String,
      endpointUrl: String,
      orderId: String,
      intent: Intent,
      policy: RetryPolicy[F]
  ): F[String] =
    Retry[F]
      .retry(policy, Retriable.Payments)(
        capturePayment(client, accessToken, endpointUrl, orderId, intent)
      )
      .adaptError { case e =>
        PaymentError(Option(e.getMessage).getOrElse("Unknown"))
      }
}

object PayPalClientService {
  def make[F[_]: Async: Logger] = PayPalClientService[F]()
}
