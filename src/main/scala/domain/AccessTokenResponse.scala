package domain
import io.circe.generic.semiauto.deriveDecoder
import io.circe.Encoder
import org.http4s.EntityEncoder
final case class AccessTokenResponse(
    scope: String,
    access_token: String,
    token_type: String,
    app_id: String,
    expires_in: Long,
    nonce: String
)

object AccessTokenResponse {
  implicit val accessTokenResponseDecoder = deriveDecoder[AccessTokenResponse]
}
