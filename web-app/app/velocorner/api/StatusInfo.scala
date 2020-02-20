package velocorner.api

import controllers.StartupService
import play.api.Mode
import play.api.libs.json._


object StatusInfo {

  implicit val modeFormat: Format[Mode] = Format[play.api.Mode]((json: JsValue) => {
    val modeString = json.asInstanceOf[JsString].value
    val mode = play.api.Mode.values.find(_.toString == modeString).getOrElse(play.api.Mode.Dev)
    JsSuccess(mode)
  }, (mode: play.api.Mode) => JsString(mode.toString))


  private val writes = new Writes[StatusInfo] {
    override def writes(o: StatusInfo): JsValue = {
      val baseJs: JsObject = Json.writes[StatusInfo].writes(o).as[JsObject]
      val typeJs: JsString = Writes.StringWrites.writes("Status")
      JsObject(baseJs.fields :+ ("type" -> typeJs))
    }
  }

  implicit val statusFormat: Format[StatusInfo] = Format[StatusInfo](Json.reads[StatusInfo], writes)

  def compute(applicationMode: play.api.Mode): StatusInfo = {
    val memoryTotal = sys.runtime.maxMemory()
    val memoryUsed = memoryTotal - sys.runtime.freeMemory()
    val memoryUsedPercentile = ((memoryUsed.toDouble * 100) / memoryTotal).toInt
    new StatusInfo(
      applicationMode = applicationMode,
      buildTime = velocorner.build.BuildInfo.buildTime,
      appVersion = velocorner.build.BuildInfo.version,
      scalaVersion = velocorner.build.BuildInfo.scalaVersion,
      sbtVersion = velocorner.build.BuildInfo.sbtVersion,
      scalazVersion = velocorner.build.BuildInfo.scalazVersion,
      elasticVersion = velocorner.build.BuildInfo.elasticVersion,
      playVersion = velocorner.build.BuildInfo.playVersion,
      gitHash = velocorner.build.BuildInfo.gitHash,
      memoryTotal = memoryTotal,
      memoryUsedPercentile = memoryUsedPercentile,
      upTime = StartupService.elapsedTimeText()
    )
  }
}

case class StatusInfo(
                       applicationMode: play.api.Mode,
                       buildTime: String,
                       appVersion: String,
                       scalaVersion: String,
                       sbtVersion: String,
                       scalazVersion: String,
                       elasticVersion: String,
                       playVersion: String,
                       gitHash: String,
                       memoryTotal: Long,
                       memoryUsedPercentile: Int,
                       upTime: String
                     )
