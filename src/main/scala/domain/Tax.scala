package domain

import io.circe.generic.semiauto.deriveEncoder
import io.circe.Encoder
import org.http4s.EntityEncoder

final case class Tax(urrency_code: String, value: String)
object Tax {
  implicit val taxEncoder = deriveEncoder[Tax]
}
