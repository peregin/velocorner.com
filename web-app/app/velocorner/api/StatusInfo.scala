package velocorner.api

import controllers.StartupService
import play.api.Mode
import play.api.libs.json._

object StatusInfo {

  implicit val modeFormat: Format[Mode] = Format[play.api.Mode](
    (json: JsValue) => {
      val modeString = json.asInstanceOf[JsString].value
      val mode = play.api.Mode.values.find(_.toString == modeString).getOrElse(play.api.Mode.Dev)
      JsSuccess(mode)
    },
    (mode: play.api.Mode) => JsString(mode.toString)
  )

  private val writes = new Writes[StatusInfo] {
    override def writes(o: StatusInfo): JsValue = {
      val baseJs: JsObject = Json.writes[StatusInfo].writes(o).as[JsObject]
      val typeJs: JsString = Writes.StringWrites.writes("Status")
      JsObject(baseJs.fields :+ ("type" -> typeJs))
    }
  }

  implicit val statusFormat: Format[StatusInfo] = Format[StatusInfo](Json.reads[StatusInfo], writes)

  def compute(applicationMode: play.api.Mode, pings: Long): StatusInfo = {
    val memoryTotal = sys.runtime.maxMemory()
    val memoryUsed = memoryTotal - sys.runtime.freeMemory()
    val memoryUsedPercentile = ((memoryUsed.toDouble * 100) / memoryTotal).toInt
    new StatusInfo(
      applicationMode = applicationMode.toString,
      buildTime = velocorner.build.BuildInfo.buildTime,
      appVersion = velocorner.build.BuildInfo.version,
      javaVersion = s"${sys.props.get("java.runtime.name").getOrElse("n/a")} ${sys.props.get("java.runtime.version").getOrElse("n/a")}",
      scalaVersion = velocorner.build.BuildInfo.scalaVersion,
      sbtVersion = velocorner.build.BuildInfo.sbtVersion,
      catsVersion = velocorner.build.BuildInfo.catsVersion,
      elasticVersion = velocorner.build.BuildInfo.elasticVersion,
      playVersion = velocorner.build.BuildInfo.playVersion,
      gitHash = velocorner.build.BuildInfo.gitHash,
      memoryTotal = memoryTotal,
      memoryUsedPercentile = memoryUsedPercentile,
      upTime = StartupService.elapsedTimeText(),
      pings = pings
    )
  }
}

case class StatusInfo(
    applicationMode: String,
    buildTime: String,
    appVersion: String,
    javaVersion: String, // runtime
    scalaVersion: String,
    sbtVersion: String,
    catsVersion: String,
    elasticVersion: String,
    playVersion: String,
    gitHash: String,
    memoryTotal: Long,
    memoryUsedPercentile: Int,
    upTime: String,
    pings: Long
)
