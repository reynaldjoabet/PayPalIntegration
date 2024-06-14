package domain

import io.circe.generic.semiauto.deriveEncoder
import io.circe.Encoder
import org.http4s.EntityEncoder

sealed abstract class Category
object Category {

  case object DIGITAL_GOODS extends Category

  case object PHYSICAL_GOODS extends Category

  case object DONATION_GOODS extends Category
  implicit val catgeoryEncoder = deriveEncoder[Category]

}
