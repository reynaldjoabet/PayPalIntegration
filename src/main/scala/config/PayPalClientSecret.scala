package config
import ciris._
final case class PayPalClientSecret(value: String)

object PayPalClientSecret {
  val payPalClientSecret = env("PAYPAL_CLIENT_SECRET")
    .as[String]
    .default(
      ""
    )
    .map(PayPalClientSecret.apply)
}
