package domain

import io.circe.generic.semiauto.deriveEncoder
import io.circe.Encoder
import org.http4s.EntityEncoder

final case class PurchaseUnit(
  items: List[Item],
  amount: Amount
)

object PurchaseUnit {
  implicit val purchaseUnitEncoder = deriveEncoder[PurchaseUnit]
}
