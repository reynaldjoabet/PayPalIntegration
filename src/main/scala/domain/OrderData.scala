package domain

import scala.util.control.NoStackTrace

import domain.Item
import io.circe.generic.semiauto.deriveEncoder
import io.circe.Encoder
import org.http4s.EntityEncoder

/**
  * { 'intent': req.body.intent.toUpperCase(), 'purchase_units': [{ 'amount': { 'currency_code':
  * 'USD', 'value': '100.00' } }] };
  *
  * @param intent
  * @param purchase_units
  */

final case class OrderData(
  intent: Intent,
  purchase_units: List[PurchaseUnit]
)

object OrderData {

  implicit val orderDataEncoder = deriveEncoder[OrderData]

  sealed trait OrderOrPaymentError extends NoStackTrace {
    def cause: String
  }

  case class OrderError(cause: String)   extends OrderOrPaymentError
  case class PaymentError(cause: String) extends OrderOrPaymentError

//implicit  def orderDataEntityEncoder[F[_]:Async]:EntityEncoder[F,OrderData]=jsonEncoderOf
}
