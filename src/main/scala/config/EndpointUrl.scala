package config
import ciris._
//password: Secret[DatabasePassword]
// port: Option[UserPortNumber]
final case class EndpointUrl(value: String)

object EndpointUrl {
  val endpointUrl = env("ENDPOINT_URL")
    .as[String]
    .default("https://api-m.sandbox.paypal.com")
    .map(EndpointUrl.apply)
}
