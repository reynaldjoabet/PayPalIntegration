package domain
import io.circe.generic.semiauto.deriveCodec
import io.circe.Encoder
import org.http4s.EntityEncoder
final case class CreateOrderResponse(
    id: String,
    links: List[Link],
    status: Status
)

object CreateOrderResponse {
  implicit val createOrderResponseCodec = deriveCodec[CreateOrderResponse]
}
