package config
import ciris._
import cats.syntax.all._

final case class PayPalCredentials(
    payPalClientId: Secret[String],
    payPalClientSecret: Secret[String]
)

object PayPalCredentials {
   private [this] val clientSecret = env("PAYPAL_CLIENT_SECRET")
    .as[String]
    .default(
      ""
    )
    .secret
  

  private [this] val clientId = env("PAYPAL_CLIENT_ID")
    .as[String]
    .default(
      ""
    )
    .secret

  def credentials: ConfigValue[Effect, PayPalCredentials] =
    (clientId, clientSecret).parMapN(PayPalCredentials(_, _))

}
