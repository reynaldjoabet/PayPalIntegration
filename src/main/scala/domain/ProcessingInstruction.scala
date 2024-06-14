package domain

import io.circe.generic.semiauto.deriveEncoder
import io.circe.Encoder

sealed abstract class ProcessingInstruction

object ProcessingInstruction {

  case object ORDER_COMPLETE_ON_PAYMENT_APPROVAL extends ProcessingInstruction
  case object NO_INSTRUCTION                     extends ProcessingInstruction

  implicit val processingInstructionEncoder =
    Encoder[String].contramap[ProcessingInstruction] {
      case ORDER_COMPLETE_ON_PAYMENT_APPROVAL =>
        "ORDER_COMPLETE_ON_PAYMENT_APPROVAL"
      case NO_INSTRUCTION => "NO_INSTRUCTION"
    }

}
