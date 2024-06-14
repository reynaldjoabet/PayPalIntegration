import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import org.http4s.QueryParamDecoder

object Pagination {

  /* Necessary for decoding query parameters */
  import QueryParamDecoder._

  /* Parses out the optional offset and page size params */
  object OptionalPageSizeMatcher extends OptionalQueryParamDecoderMatcher[Int]("pageSize")
  object OptionalOffsetMatcher   extends OptionalQueryParamDecoderMatcher[Int]("offset")

}
