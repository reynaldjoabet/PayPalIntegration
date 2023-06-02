package domain
import io.circe.Encoder
import org.http4s.EntityEncoder
import io.circe.generic.semiauto.deriveEncoder
final case class Amount(
    currency_code: String,
    value: String,
    breakdown: Breakdown
)

object Amount {
  implicit val amountEncoder = deriveEncoder[Amount]
}
