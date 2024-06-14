package config

import cats.syntax.all._

import ciris._

final case class PayPalCredentials(
  payPalClientId: Secret[String],
  payPalClientSecret: Secret[String]
)

object PayPalCredentials {

  private[this] val clientSecret: ConfigValue[Effect, Secret[String]] =
    env("PAYPAL_CLIENT_SECRET")
      .as[String]
      .default(
        ""
      )
      .secret

  private[this] val clientId: ConfigValue[Effect, Secret[String]] =
    env("PAYPAL_CLIENT_ID")
      .as[String]
      .default(
        ""
      )
      .secret

  def credentials: ConfigValue[Effect, PayPalCredentials] =
    (clientId, clientSecret).parMapN(PayPalCredentials(_, _))

}
