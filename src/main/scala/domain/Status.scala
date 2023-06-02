package domain
import io.circe.generic.semiauto.deriveEncoder
import io.circe.{Encoder, Decoder}
import org.http4s.EntityEncoder

sealed abstract class Status

object Status {
  case object CREATED extends Status
  case object SAVED extends Status
  case object APPROVED extends Status
  case object VOIDED extends Status
  case object COMPLETED extends Status
  case object PAYER_ACTION_REQUIRED extends Status

  implicit val statusEncoder: Encoder[Status] =
    Encoder[String].contramap[Status] {
      case CREATED               => "CREATED"
      case SAVED                 => "SAVED"
      case APPROVED              => "APPROVED "
      case VOIDED                => "VOIDED "
      case COMPLETED             => "COMPLETED"
      case PAYER_ACTION_REQUIRED => "PAYER_ACTION_REQUIRED"
    }
  implicit val decoderStatus = Decoder[String].emap[Status] {
    case "CREATED"               => Right(CREATED)
    case "SAVED"                 => Right(SAVED)
    case "APPROVED "             => Right(APPROVED)
    case "VOIDED "               => Right(VOIDED)
    case "COMPLETED"             => Right(COMPLETED)
    case "PAYER_ACTION_REQUIRED" => Right(PAYER_ACTION_REQUIRED)
  }
}
