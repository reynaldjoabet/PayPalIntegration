package domain

import io.circe.generic.semiauto.deriveDecoder
import io.circe.Encoder
import org.http4s.EntityEncoder

final case class CapturePaymentRequest(
  intent: Intent,
  order_id: String
)

object CapturePaymentRequest {

  implicit val capturePaymentRequestDecoder =
    deriveDecoder[CapturePaymentRequest]

}
