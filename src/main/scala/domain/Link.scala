package domain
import io.circe.generic.semiauto.deriveCodec
import org.http4s.EntityEncoder
import io.circe.Codec

final case class Link(
    href: String,
    rel: String,
    method: String
)

object Link {
  implicit val linkCodec: Codec[Link] = deriveCodec[Link]

}
