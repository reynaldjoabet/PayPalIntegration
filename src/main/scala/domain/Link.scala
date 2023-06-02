package domain
import io.circe.generic.semiauto.deriveCodec
import io.circe.Encoder
import org.http4s.EntityEncoder
final case class Link(
    href: String,
    rel: String,
    method: String
)

object Link {
  implicit val linkCodec = deriveCodec[Link]

}
