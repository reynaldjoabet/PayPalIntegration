package domain

final case class Payee(
    emailAddress: Option[String],
    merchantId: Option[String]
)

object Payee {}
