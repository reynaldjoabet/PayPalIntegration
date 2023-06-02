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

final case class PayPalApiRoutes(
    clientService: PayPalClientService[IO],
    client: Client[IO]
) extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case request @ POST -> Root / "create_order" =>
      (for {
        clientSecret <- PayPalClientSecret.payPalClientSecret.load[IO]
        clientId <- PayPalClientId.payPalClientId.load[IO]
        endpointUrl <- EndpointUrl.endpointUrl.load[IO]
        accessTokenResponse <- clientService
          .fetchAccessToken(
            client,
            clientId.value,
            clientSecret.value,
            endpointUrl.value
          )
        // .flatTap(IO.println)
        order <- clientService.createOrder(
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
          )
        )
      } yield order)
        .flatMap(data => Ok(data)) // apply method produces F[Response[G]]
        .recoverWith { case exception =>
          BadRequest(IO(exception.toString()).flatTap(IO.println))
        }

    case request @ POST -> Root / "complete_order" =>
      (for {
        payload <- request.as[CompleteOrderRequest] // .flatTap(IO.println)
        clientSecret <- PayPalClientSecret.payPalClientSecret.load[IO]
        clientId <- PayPalClientId.payPalClientId.load[IO]
        endpointUrl <- EndpointUrl.endpointUrl.load[IO]
        accessTokenResponse <- clientService
          .fetchAccessToken(
            client,
            clientId.value,
            clientSecret.value,
            endpointUrl.value
          )
          .flatTap(IO.println)
        orderResponse <- clientService.completeOrder(
          client,
          accessTokenResponse.access_token,
          endpointUrl.value,
          payload.order_id,
          payload.intent
        )
      } yield orderResponse)
        .flatMap(data => Ok(data)) // apply method produces F[Response[G]]
        .recoverWith { case exception =>
          BadRequest(exception.toString()).flatTap(IO.println)
        }
  }
}
