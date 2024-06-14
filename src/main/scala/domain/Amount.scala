package domain

import io.circe.generic.semiauto.deriveEncoder
import io.circe.Encoder
import org.http4s.EntityEncoder

final case class Amount(
  currency_code: String,
  value: String,
  breakdown: Breakdown
)

object Amount {
  implicit val amountEncoder = deriveEncoder[Amount]
}
