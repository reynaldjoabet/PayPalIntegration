package domain
import io.circe.generic.semiauto.deriveDecoder
import io.circe.Encoder
import org.http4s.EntityEncoder
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import io.circe.generic.extras.Configuration
final case class AccessTokenResponse(
    scope: String,
    access_token: String,
    token_type: String,
    app_id: String,
    expires_in: Long,
    nonce: String
)

object AccessTokenResponse {
  implicit val configuration: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  // implicit val accessTokenResponseDecoder1= deriveConfiguredEncoder[AccessTokenResponse]
  implicit val accessTokenResponseDecoder = deriveDecoder[AccessTokenResponse]
}
