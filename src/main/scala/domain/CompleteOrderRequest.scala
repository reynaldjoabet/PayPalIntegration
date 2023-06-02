package domain
import io.circe.generic.semiauto.deriveDecoder
import io.circe.Encoder
import org.http4s.EntityEncoder

final case class CompleteOrderRequest(
    intent: Intent,
    order_id: String
)

object CompleteOrderRequest {
  implicit val completeOrderResponseDecoder =
    deriveDecoder[CompleteOrderRequest]
}
