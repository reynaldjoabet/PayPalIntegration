package routes

import scala.concurrent.duration._

import cats.data.Kleisli
import cats.effect.kernel.Async
import cats.effect.std.Console
import cats.effect.syntax.all._
import cats.effect.IO
import cats.syntax.all._

import client.PayPalClientService
import config._
import domain._
import org.http4s.circe._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.dsl._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.server.middleware.authentication.DigestAuth
import org.http4s.server.staticcontent.webjarServiceBuilder
//import org.typelevel.jawn.Syntax.checkFile
import org.http4s.server.AuthMiddleware
import org.http4s.HttpRoutes
import org.http4s.MediaType
import org.http4s.Response
import org.typelevel.log4cats.Logger
import retry.RetryPolicy

final case class PayPalApiRoutes[F[_]: Async: Logger: Console](
  clientService: PayPalClientService[F],
  client: Client[F],
  credentials: PayPalCredentials,
  endpointUrl: EndpointUrl,
  policy: RetryPolicy[F]
) extends Http4sDsl[F] {

  val fileTypes = List(".js", ".css", ".map", ".html", ".webm")

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
//Kleisli.apply()
    case request @ GET -> Root / path if fileTypes.exists(path.endsWith) => ???
    case request @ POST -> Root / "create_order" =>
      (for {
        accessTokenResponse <- clientService.fetchAccessToken(
                                 client,
                                 endpointUrl.value,
                                 credentials
                               )
        // .flatTap(IO.println)
        order <- clientService
                   .createOrderWithRetry(
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
                   .uncancelable // one should not be able to cancel the creation of an order
        // .onCancel(Async[F].unit)
      } yield order)
        .flatMap(data => Ok(data)) // apply method produces F[Response[G]]
        .recoverWith { case exception =>
          BadRequest(exception.toString()).flatTap(Console[F].println)
        }

    case request @ POST -> Root / "capture_order" =>
      (for {
        payload <- request.as[CapturePaymentRequest] // .flatTap(IO.println)
        accessTokenResponse <- clientService.fetchAccessToken(
                                 client,
                                 endpointUrl.value,
                                 credentials
                               ) // .timeout(45.millis )
        // .flatTap(.println)
        orderResponse <- clientService
                           .capturePaymentWithRetry(
                             client,
                             accessTokenResponse.access_token,
                             endpointUrl.value,
                             payload.order_id,
                             payload.intent,
                             policy
                           )
                           .uncancelable // makes this entire IO uncancelable
      } yield orderResponse)
        .flatMap(data => Ok(data)) // apply method produces F[Response[G]]
        .recoverWith { case exception =>
          BadRequest(exception.getMessage()).flatTap(Console[F].println)
        }.handleErrorWith(defaultErrorsMappings)
  }
//IO.uncancelable(unMask=>IO.println(0).!>(unMask(IO.println(89))))
//MediaType.audio

  private def defaultErrorsMappings: PartialFunction[Throwable, F[Response[F]]] = {
    case e: Exceptions.AlreadyInUse => Conflict(e.message)
    case e: Exceptions.NotFound     => NotFound(e.message)
    case e: Exceptions.BadRequest   => BadRequest(e.message)
    case e: Exceptions.Unauthorized => Forbidden(e.message) // Unauthorized.apply(e.message)
    case _                          => InternalServerError()
  }

}
