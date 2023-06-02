package config
import ciris._
final case class PayPalClientId(value: String)

object PayPalClientId {
  val payPalClientId = env("PAYPAL_CLIENT_ID")
    .as[String]
    .default(
      ""
    )
    .map(PayPalClientId.apply)
}
