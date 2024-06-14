package domain

import io.circe.generic.semiauto.deriveEncoder
import io.circe.Encoder
import org.http4s.EntityEncoder

final case class BreakdownAmount(currency_code: String, value: String)

object BreakdownAmount {
  implicit val unitAmountEncoder = deriveEncoder[BreakdownAmount]
}
