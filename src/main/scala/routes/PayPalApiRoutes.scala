package routes
import client.PayPalClientService
import org.http4s.client.dsl._
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.implicits._
import cats.effect.kernel.Async
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.client.Client
import org.http4s.circe.CirceEntityCodec._
import domain._
import config._
import cats.effect.IO
import org.typelevel.log4cats.Logger
import retry.RetryPolicy
import cats.syntax.all._
import cats.effect.std.Console

final case class PayPalApiRoutes[F[_]: Async: Logger: Console](
    clientService: PayPalClientService[F],
    client: Client[F],
    credentials:PayPalCredentials,
    endpointUrl: EndpointUrl,
    policy: RetryPolicy[F]
) extends Http4sDsl[F] {
  val routes: HttpRoutes[F] = HttpRoutes.of[F] {

    case request @ POST -> Root / "create_order" =>
      (for {
        accessTokenResponse <- clientService
          .fetchAccessToken(
            client,
            endpointUrl.value,
            credentials
          )
        // .flatTap(IO.println)
        order <- clientService.createOrderWithRetry(
          client,
          accessTokenResponse.access_token,
          endpointUrl.value,
          OrderData(
            Intent.CAPTURE,
            List(
              PurchaseUnit(
                items = List(Item("T-shirt", "2", UnitAmount("USD", "300.00"))),
                Amount(
                  "USD",
                  "600.00",
                  Breakdown(BreakdownAmount("USD", "600"))
                )
              )
            )
          ),
        policy
        )
      } yield order)
        .flatMap(data => Ok(data)) // apply method produces F[Response[G]]
        .recoverWith { case exception =>
          BadRequest(exception.toString()).flatTap(Console[F].println)
        }

    case request @ POST -> Root / "capture_order" =>
      (for {
        payload <- request.as[CapturePaymentRequest] // .flatTap(IO.println)
        accessTokenResponse <- clientService
          .fetchAccessToken(
            client,
            endpointUrl.value,
            credentials
          )
        // .flatTap(.println)
        orderResponse <- clientService.capturePaymentWithRetry(
          client,
          accessTokenResponse.access_token,
          endpointUrl.value,
          payload.order_id,
          payload.intent,
          policy
        )
      } yield orderResponse)
        .flatMap(data => Ok(data)) // apply method produces F[Response[G]]
        .recoverWith { case exception =>
          BadRequest(exception.getMessage()).flatTap(Console[F].println)
        }
  }

}
