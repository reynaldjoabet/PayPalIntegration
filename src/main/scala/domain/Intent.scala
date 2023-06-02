package domain
import io.circe.generic.semiauto.deriveCodec
import io.circe.{Encoder, Decoder}
import org.http4s.EntityEncoder
import org.http4s.EntityDecoder
sealed abstract class Intent
object Intent {

  // implicit val intentCodec=deriveCodec[Intent]
  implicit val intentEncoder: Encoder[Intent] =
    Encoder[String].contramap[Intent] {
      case CAPTURE   => "CAPTURE"
      case AUTHORIZE => "AUTHORIZE"
    }

  implicit val intentDecoder: Decoder[Intent] = Decoder[String].emap[Intent] {
    case "CAPTURE"   => Right(CAPTURE)
    case "AUTHORIZE" => Right(AUTHORIZE)
  }
  final case object CAPTURE extends Intent
  final case object AUTHORIZE extends Intent
}
