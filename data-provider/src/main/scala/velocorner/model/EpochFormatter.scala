package velocorner.model

import org.joda.time.DateTime
import play.api.libs.json._

object EpochFormatter {

  // epoch to DateTime nad vice versa
  def create = Format[DateTime](new Reads[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] = {
      val epoch = json.asInstanceOf[JsNumber].value.toLong * 1000
      JsSuccess(new DateTime(epoch))
    }
  }, JodaWrites.JodaDateTimeNumberWrites)
}
