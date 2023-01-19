package velocorner.crawler

import velocorner.api.Money

import scala.util.{Failure, Success, Try}

object PriceParser {

  val pricePattern = "([\\d\\s.,]*\\d)\\s*(\\S*)".r

  /**
   * patterns:
   * 54.29€
   * 1,629.08€
   * 1'010.70 CHF
   */
  def parse(amountCcy: String): Money = amountCcy
    .replace(",", "")
    .replace("'", "") match {
    case pricePattern(amount, currency) =>
      Try(Money(BigDecimal(amount), normalizeCurrency(currency))) match {
        case Success(value) => value
        case Failure(err)   => throw new IllegalArgumentException(s"unable to parse price $amountCcy, because $err")
      }
    case other => throw new IllegalArgumentException(s"invalid price pattern $other")
  }

  def normalizeCurrency(c: String): String = c match {
    case "€"   => "EUR"
    case "$"   => "USD"
    case "£"   => "GBP"
    case other => other
  }
}
