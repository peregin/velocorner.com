package velocorner.model

import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}
import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import play.api.libs.json.{Format, JodaReads, JodaWrites, JsError, JsNumber, JsPath, JsResult, JsString, JsSuccess, JsValue, JsonValidationError, Reads}

/**
 * Created by levi on 24/12/15.
 */
object DateTimePattern {

  // used UTC, otherwise DST can cause problems reading the time in a different zone.
  val longFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"

  val shortFormat = "yyyy-MM-dd"

  def createLongFormatter: Format[DateTime] =
    Format[DateTime](jodaDateReadsUTC(DateTimePattern.longFormat), JodaWrites.jodaDateWrites(DateTimePattern.longFormat))

  def createShortFormatter: Format[LocalDate] = Format[LocalDate](
    JodaReads.jodaLocalDateReads(DateTimePattern.shortFormat),
    JodaWrites.jodaLocalDateWrites(DateTimePattern.shortFormat)
  )

  private def jodaDateReadsUTC(pattern: String, corrector: String => String = identity): Reads[DateTime] = new Reads[DateTime] {
    val df =
      (if (pattern == "") ISODateTimeFormat.dateOptionalTimeParser else DateTimeFormat.forPattern(pattern)).withZone(DateTimeZone.UTC)

    def reads(json: JsValue): JsResult[DateTime] = json match {
      case JsNumber(d) => JsSuccess(new DateTime(d.toLong))
      case JsString(s) =>
        parseDate(corrector(s)) match {
          case Some(d) => JsSuccess(d)
          case _       => JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.jodadate.format", pattern))))
        }
      case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.date"))))
    }

    private def parseDate(input: String): Option[DateTime] =
      scala.util.control.Exception.nonFatalCatch[DateTime].opt(DateTime.parse(input, df))
  }
}
