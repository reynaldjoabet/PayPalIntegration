package domain

import io.circe.generic.semiauto.deriveEncoder
import io.circe.Encoder
import org.http4s.EntityEncoder

//checked and complete
final case class Item(
  name: String,
  quantity: String,
  unit_amount: UnitAmount
)

object Item {
  implicit val itemEncoder = deriveEncoder[Item]
}
