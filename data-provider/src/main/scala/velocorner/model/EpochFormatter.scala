package velocorner.model

import org.joda.time.DateTime
import play.api.libs.json._

object EpochFormatter {

  // epoch to DateTime and vice versa
  def create: Format[DateTime] = Format[DateTime](
    (json: JsValue) => {
      val epoch = json.asInstanceOf[JsNumber].value.toLong * 1000
      JsSuccess(new DateTime(epoch))
    },
    (d: DateTime) => JsNumber(d.getMillis / 1000)
  )
}
