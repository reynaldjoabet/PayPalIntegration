package domain
import io.circe.Encoder
import org.http4s.EntityEncoder
import io.circe.generic.semiauto.deriveEncoder
final case class Tax(urrency_code: String, value: String)
object Tax {
  implicit val taxEncoder = deriveEncoder[Tax]
}
