package domain
import io.circe.Encoder
import org.http4s.EntityEncoder
import io.circe.generic.semiauto.deriveEncoder
final case class Breakdown(item_total: BreakdownAmount)

object Breakdown {
  implicit val breakdownEncoder = deriveEncoder[Breakdown]
}
