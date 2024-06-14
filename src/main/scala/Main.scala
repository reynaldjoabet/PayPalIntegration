import scala.concurrent.duration._

import cats.effect._
import cats.effect.ExitCode

import client.PayPalClientService
import com.comcast.ip4s._
import config._
import io.circe
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.server.defaults.Banner
import org.http4s.server.middleware.HSTS
import org.http4s.server.middleware.RequestLogger
import org.http4s.server.Server
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.LoggerName
import retry.RetryPolicies._
import retry.RetryPolicy
import routes._

object Main extends IOApp {

  // private val errorhandler: PartialFunction[Throwable, IO[Response[IO]]] = { case th: Throwable =>
  // th.printStackTrace()
  // std.Console[IO].error(s"InternalServerError: $th") *> InternalServerError(
  //  s"InternalServerError: $th"
  //  )
  // }

  val hstsHeader = `Strict-Transport-Security`.unsafeFromDuration(
    30.days,
    includeSubDomains = true,
    preload = true
  )

//implicit val loggerName=LoggerName("name")
  implicit private val logger = Slf4jLogger.getLogger[IO]

  private val retryPolicy: RetryPolicy[IO] =
    limitRetries[IO](5).join(exponentialBackoff[IO](1.second))

  private def showEmberBanner[F[_]: Logger](s: Server): F[Unit] =
    Logger[F].info(
      s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}"
    )

  override def run(args: List[String]): IO[ExitCode] = (for {
    client      <- EmberClientBuilder.default[IO].build
    credentials <- Resource.eval(PayPalCredentials.credentials.load[IO])
    endpointUrl <- Resource.eval(EndpointUrl.endpointUrl.load[IO])
    _ <- EmberServerBuilder
           .default[IO]
           .withHttpApp(
             RequestLogger.httpApp(true, true)(
               PayPalApiRoutes(
                 PayPalClientService.make[IO],
                 client,
                 credentials,
                 endpointUrl,
                 retryPolicy
               ).routes.orNotFound
             )
           )
           .withPort(port"8080")
           .withHost(host"127.0.0.1")
           .withHttp2
           // .withErrorHandler()

           // .withHostOption()
           .build.evalTap(showEmberBanner[IO])

  } yield ()).useForever.as(ExitCode.Success)

}
