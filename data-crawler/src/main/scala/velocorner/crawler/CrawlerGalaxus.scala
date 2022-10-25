package velocorner.crawler

import cats.implicits._
import cats.effect.Async
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.{Header, Headers, Method}
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits.http4sLiteralsSyntax
import org.typelevel.ci.CIString
import velocorner.api.Money
import velocorner.api.brand.Marketplace.Galaxus
import velocorner.api.brand.{Brand, Marketplace, ProductDetails}
import velocorner.crawler.CrawlerGalaxus.SearchResponse

import java.net.URLEncoder

object CrawlerGalaxus {

  val baseUrl = Galaxus.url.stripSuffix("/")

  case class GalaxusImage(url: String)

  object GalaxusImage {
    implicit val codec: Codec[GalaxusImage] = deriveCodec
  }

  case class GalaxusProduct(
      name: String,
      nameProperties: String,
      productTypeName: String,
      productId: Long,
      brandName: String,
      averageRating: Option[Double],
      images: List[GalaxusImage]
  )

  object GalaxusProduct {
    implicit val codec: Codec[GalaxusProduct] = deriveCodec
  }

  case class GalaxusPrice(amountIncl: Double, currency: String)

  object GalaxusPrice {
    implicit val codec: Codec[GalaxusPrice] = deriveCodec
  }

  case class GalaxusOffer(price: GalaxusPrice, productId: Long)

  object GalaxusOffer {
    implicit val codec: Codec[GalaxusOffer] = deriveCodec
  }

  case class Result(offer: GalaxusOffer, product: GalaxusProduct)

  object Result {
    implicit val codec: Codec[Result] = deriveCodec
  }

  case class Products(results: List[Result])

  object Products {
    implicit val codec: Codec[Products] = deriveCodec
  }
  case class Search(products: Products)

  object Search {
    implicit val codec: Codec[Search] = deriveCodec
  }

  case class Data(search: Search)
  object Data {
    implicit val codec: Codec[Data] = deriveCodec
  }

  case class SearchResponse(data: Data) {

    def convert(s: String): String = s.toLowerCase.replace(' ', '-')
    def toApi(): List[ProductDetails] = data.search.products.results.map { res =>
      val p = res.product
      ProductDetails(
        market = Galaxus,
        brand = Brand(name = p.brandName, logoUrl = none).some,
        name = p.name,
        description = none,
        price = Money(res.offer.price.amountIncl, res.offer.price.currency),
        imageUrl = p.images.headOption.map(_.url).getOrElse("n/a"),
        productUrl = baseUrl + "/en/product/" + convert(p.name) + "-" + convert(p.nameProperties) + "-" + convert(
          p.productTypeName
        ) + "-" + p.productId,
        reviewStars = p.averageRating.getOrElse(0),
        isNew = false,
        onSales = false,
        onStock = true
      )
    }
  }

  object SearchResponse {
    implicit val codec: Codec[SearchResponse] = deriveCodec
  }
}
class CrawlerGalaxus[F[_]: Async](client: Client[F]) extends Crawler[F] with Http4sClientDsl[F] {

  override def market(): Marketplace = Galaxus

  def payload(term: String, limit: Int): String =
    s"""[
      |  {
      |    "operationName": "ENTER_SEARCH",
      |    "variables": {
      |      "limit": $limit,
      |      "offset": 0,
      |      "query": "$term",
      |      "filters": [],
      |      "sortOrder": null,
      |      "include": [
      |        "bra",
      |        "pt",
      |        "pr",
      |        "off"
      |      ],
      |      "searchQueryId": "223f95fc-f7d0-4b88-99d1-4220a43a3a35",
      |      "ltrEnabled": true,
      |      "siteId": null
      |    },
      |    "query": "query ENTER_SEARCH($$query: String!, $$sortOrder: ProductSort, $$limit: Int = ${limit-1}, $$offset: Int = 0, $$filters: [SearchFilter], $$include: [String!], $$exclude: [String!], $$searchQueryId: String, $$rewriters: [String!], $$ltrEnabled: Boolean, $$siteId: String) {\\n  search(\\n    query: $$query\\n    filters: $$filters\\n    searchQueryId: $$searchQueryId\\n    rewriters: $$rewriters\\n    ltrEnabled: $$ltrEnabled\\n    siteId: $$siteId\\n  ) {\\n    products(limit: $$limit, offset: $$offset, sortOrder: $$sortOrder) {\\n      total\\n      hasMore\\n      nextOffset\\n      results {\\n        ...ProductSearchResult\\n        __typename\\n      }\\n      __typename\\n    }\\n    filters(include: $$include, exclude: $$exclude) {\\n      product {\\n        identifier\\n        name\\n        filterType\\n        score\\n        tooltip {\\n          ...FilterTooltipResult\\n          __typename\\n        }\\n        ...CheckboxSearchFilterResult\\n        ...RangeSearchFilterResult\\n        __typename\\n      }\\n      quickFilter {\\n        options {\\n          filterType\\n          filterIdentifier\\n          filterName\\n          filterOptionIdentifier\\n          filterOptionName\\n          __typename\\n        }\\n        __typename\\n      }\\n      __typename\\n    }\\n    magazinePages(limit: 3) {\\n      ids {\\n        id\\n        score\\n        __typename\\n      }\\n      __typename\\n    }\\n    authors(limit: 3) {\\n      ids {\\n        id\\n        score\\n        __typename\\n      }\\n      __typename\\n    }\\n    discussions(limit: 3) {\\n      ids {\\n        id\\n        score\\n        __typename\\n      }\\n      __typename\\n    }\\n    questions(limit: 3) {\\n      ids {\\n        id\\n        score\\n        __typename\\n      }\\n      __typename\\n    }\\n    ratings(limit: 3) {\\n      ids {\\n        id\\n        score\\n        __typename\\n      }\\n      total\\n      __typename\\n    }\\n    productTypes(limit: 24) {\\n      total\\n      results {\\n        id\\n        name\\n        primarySynonyms\\n        isVisible\\n        description\\n        metaDescription\\n        imageUrl\\n        searchScore\\n        __typename\\n      }\\n      __typename\\n    }\\n    brands(limit: 24) {\\n      total\\n      results {\\n        id\\n        title\\n        searchScore\\n        __typename\\n      }\\n      __typename\\n    }\\n    _meta {\\n      queryInfo {\\n        correctedQuery\\n        didYouMeanQuery\\n        lastProductSearchPass\\n        executedSearchTerm\\n        testGroup\\n        isManagedQuery\\n        isRerankedQuery\\n        __typename\\n      }\\n      redirectionUrl\\n      portalReferral {\\n        productCount\\n        portalName\\n        url\\n        productImageUrls\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\\nfragment ProductSearchResult on ProductSearchResultItem {\\n  searchScore\\n  mandatorSpecificData {\\n    ...ProductMandatorSpecific\\n    __typename\\n  }\\n  product {\\n    ...ProductMandatorIndependent\\n    __typename\\n  }\\n  offer {\\n    ...ProductOffer\\n    __typename\\n  }\\n  __typename\\n}\\n\\nfragment FilterTooltipResult on FilterTooltip {\\n  text\\n  moreInformationLink\\n  __typename\\n}\\n\\nfragment CheckboxSearchFilterResult on CheckboxSearchFilter {\\n  options {\\n    identifier\\n    name\\n    productCount\\n    score\\n    referenceValue {\\n      value\\n      unit {\\n        abbreviation\\n        __typename\\n      }\\n      __typename\\n    }\\n    preferredValue {\\n      value\\n      unit {\\n        abbreviation\\n        __typename\\n      }\\n      __typename\\n    }\\n    tooltip {\\n      ...FilterTooltipResult\\n      __typename\\n    }\\n    __typename\\n  }\\n  __typename\\n}\\n\\nfragment RangeSearchFilterResult on RangeSearchFilter {\\n  referenceMin\\n  preferredMin\\n  referenceMax\\n  preferredMax\\n  referenceStepSize\\n  preferredStepSize\\n  rangeMergeInfo {\\n    isBottomMerged\\n    isTopMerged\\n    __typename\\n  }\\n  referenceUnit {\\n    abbreviation\\n    __typename\\n  }\\n  preferredUnit {\\n    abbreviation\\n    __typename\\n  }\\n  rangeFilterDataPoint {\\n    ...RangeFilterDataPointResult\\n    __typename\\n  }\\n  __typename\\n}\\n\\nfragment ProductMandatorSpecific on MandatorSpecificData {\\n  isBestseller\\n  isDeleted\\n  showroomSites\\n  sectorIds\\n  hasVariants\\n  __typename\\n}\\n\\nfragment ProductMandatorIndependent on ProductV2 {\\n  id\\n  productId\\n  name\\n  nameProperties\\n  productTypeId\\n  productTypeName\\n  brandId\\n  brandName\\n  averageRating\\n  totalRatings\\n  totalQuestions\\n  isProductSet\\n  images {\\n    url\\n    height\\n    width\\n    __typename\\n  }\\n  energyEfficiency {\\n    energyEfficiencyColorType\\n    energyEfficiencyLabelText\\n    energyEfficiencyLabelSigns\\n    energyEfficiencyImage {\\n      url\\n      height\\n      width\\n      __typename\\n    }\\n    __typename\\n  }\\n  seo {\\n    seoProductTypeName\\n    seoNameProperties\\n    productGroups {\\n      productGroup1\\n      productGroup2\\n      productGroup3\\n      productGroup4\\n      __typename\\n    }\\n    gtin\\n    __typename\\n  }\\n  smallDimensions\\n  basePrice {\\n    priceFactor\\n    value\\n    __typename\\n  }\\n  __typename\\n}\\n\\nfragment ProductOffer on OfferV2 {\\n  id\\n  productId\\n  offerId\\n  shopOfferId\\n  price {\\n    amountIncl\\n    amountExcl\\n    currency\\n    __typename\\n  }\\n  deliveryOptions {\\n    mail {\\n      classification\\n      futureReleaseDate\\n      __typename\\n    }\\n    pickup {\\n      siteId\\n      classification\\n      futureReleaseDate\\n      __typename\\n    }\\n    detailsProvider {\\n      productId\\n      offerId\\n      quantity\\n      type\\n      __typename\\n    }\\n    __typename\\n  }\\n  label\\n  labelType\\n  type\\n  volumeDiscountPrices {\\n    minAmount\\n    price {\\n      amountIncl\\n      amountExcl\\n      currency\\n      __typename\\n    }\\n    isDefault\\n    __typename\\n  }\\n  salesInformation {\\n    numberOfItems\\n    numberOfItemsSold\\n    isEndingSoon\\n    validFrom\\n    __typename\\n  }\\n  incentiveText\\n  isIncentiveCashback\\n  isNew\\n  isSalesPromotion\\n  hideInProductDiscovery\\n  canAddToBasket\\n  hidePrice\\n  insteadOfPrice {\\n    type\\n    price {\\n      amountIncl\\n      amountExcl\\n      currency\\n      __typename\\n    }\\n    __typename\\n  }\\n  minOrderQuantity\\n  __typename\\n}\\n\\nfragment RangeFilterDataPointResult on RangeFilterDataPoint {\\n  count\\n  referenceValue {\\n    value\\n    unit {\\n      abbreviation\\n      __typename\\n    }\\n    __typename\\n  }\\n  preferredValue {\\n    value\\n    unit {\\n      abbreviation\\n      __typename\\n    }\\n    __typename\\n  }\\n  __typename\\n}\\n"
      |  }
      |]
      |""".stripMargin

  override def products(searchTerm: String, limit: Int): F[List[ProductDetails]] = {
    val headers: Headers = Headers(
      Header.Raw(CIString("authority"), "www.galaxus.ch"),
      Header.Raw(CIString("referer"), s"https://www.galaxus.ch/search?q=${URLEncoder.encode(searchTerm, "UTF-8")}"),
      Header.Raw(CIString("user-agent"), "Mozilla/5.0"),
      Header.Raw(CIString("accept"), "*/*"),
      Header.Raw(CIString("content-type"), "application/json"),
      Header.Raw(CIString("x-dg-language"), "en-US"),
      Header.Raw(CIString("x-dg-country"), "ch"),
      Header.Raw(CIString("x-dg-datadog-route"), "/api/graphql/csr-enter-search"),
      Header.Raw(CIString("x-dg-routename"), "/search")
    )
    val uri = uri"https://www.galaxus.ch/api/graphql/enter-search"
    val request = Method.POST(payload(searchTerm, limit), uri, headers)
    for {
      res <- client.expect[List[SearchResponse]](request)
    } yield res.flatMap(_.toApi())
  }
}
