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
import domain._
import io.circe.syntax._
import org.http4s.client.middleware.RequestLogger
final case class PayPalClientService[F[_]: Async]() extends Http4sClientDsl[F] {

  def fetchAccessToken(
      client: Client[F],
      clientId: String,
      clientSecret: String,
      endpointUrl: String // Uri.uri("https://api-m.sandbox.paypal.com")
  ): F[AccessTokenResponse] = {

    val request = Method.POST(
      UrlForm(
        "grant_type" -> "client_credentials"
      ),
      Uri.unsafeFromString(endpointUrl + "/v1/oauth2/token"),
      Accept(MediaType.application.json),
      Authorization(
        BasicCredentials(clientId, clientSecret)
      ) // should it be changed to base64??
    )
    client.expect(request)(jsonOf[F, AccessTokenResponse])

  }

  def completeOrder(
      client: Client[F],
      accessToken: String,
      endpointUrl: String,
      orderId: String,
      intent: Intent
  ) = {

    val request = Method.POST.apply(
      Uri.unsafeFromString(
        endpointUrl + "/v2/checkout/orders/" + orderId + "/capture"
      ),
      Accept(MediaType.application.json),
      Authorization(Credentials.Token(AuthScheme.Bearer, accessToken))
    ).withEmptyBody
    client.expect(request)(jsonOf[F, String])
    

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
    client.expect(request)(jsonOf[F, CreateOrderResponse])

  }

}

object PayPalClientService {
  def make[F[_]: Async] = PayPalClientService[F]
}
