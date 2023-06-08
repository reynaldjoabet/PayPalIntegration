import cats.effect.ExitCode
import cats.effect._
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s._
import org.http4s.server.defaults.Banner
import org.http4s.server.Server
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.LoggerName
import io.circe
import routes._
import org.http4s.ember.client.EmberClientBuilder
import client.PayPalClientService
import org.http4s.server.middleware.RequestLogger
import retry.RetryPolicies._
import retry.RetryPolicy
import scala.concurrent.duration._
import config._

object Main extends IOApp {
//implicit val loggerName=LoggerName("name")
  private implicit val logger = Slf4jLogger.getLogger[IO]

  private val retryPolicy: RetryPolicy[IO] =
    limitRetries[IO](5) join exponentialBackoff[IO](1.second)
  private def showEmberBanner[F[_]: Logger](s: Server): F[Unit] =
    Logger[F].info(
      s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}"
    )

  override def run(args: List[String]): IO[ExitCode] = (for{
      client<-EmberClientBuilder
      .default[IO]
      .build
      credentials<-Resource.eval(PayPalCredentials.credentials.load[IO])
      endpointUrl<-Resource.eval(EndpointUrl.endpointUrl.load[IO])
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
          //.withHostOption()
          .build
          .evalTap(showEmberBanner[IO])

    } yield ()).useForever.as(ExitCode.Success)
    
            
             
}
