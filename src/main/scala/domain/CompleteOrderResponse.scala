package domain
import io.circe.generic.semiauto.deriveEncoder
import io.circe.Encoder
import org.http4s.EntityEncoder
final case class CompleteOrderResponse()

object CompleteOrderResponse {
  implicit val completeOrderResponseEncoder =
    deriveEncoder[CompleteOrderResponse]
}
