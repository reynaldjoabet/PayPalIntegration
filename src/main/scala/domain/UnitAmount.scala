package domain
import io.circe.generic.semiauto.deriveEncoder
import io.circe.Encoder
import org.http4s.EntityEncoder
final case class UnitAmount(currency_code: String, value: String)

object UnitAmount {
  implicit val unitAmountEncoder = deriveEncoder[UnitAmount]
}
