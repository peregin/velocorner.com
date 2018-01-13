package velocorner.model

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json.{Format, JodaReads, JodaWrites}

/**
  * Created by levi on 24/12/15.
  */
object DateTimePattern {

  val longFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"

  val shortFormat = "yyyy-MM-dd"

  def createLongFormatter = Format[DateTime](JodaReads.jodaDateReads(DateTimePattern.longFormat), JodaWrites.jodaDateWrites(DateTimePattern.longFormat))

  def createShortFormatter = Format[LocalDate](JodaReads.jodaLocalDateReads(DateTimePattern.shortFormat), JodaWrites.jodaLocalDateWrites(DateTimePattern.shortFormat))
}
