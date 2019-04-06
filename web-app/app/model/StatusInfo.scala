package model

import play.api.libs.json._


object StatusInfo {

  implicit val modeFormat = Format[play.api.Mode]((json: JsValue) => {
    val modeString = json.asInstanceOf[JsString].value
    val mode = play.api.Mode.values.find(_.toString == modeString).getOrElse(play.api.Mode.Dev)
    JsSuccess(mode)
  }, (mode: play.api.Mode) => JsString(mode.toString))


  val writes = new Writes[StatusInfo] {
    override def writes(o: StatusInfo): JsValue = {
      val baseJs: JsObject = Json.writes[StatusInfo].writes(o).as[JsObject]
      val typeJs: JsString = Writes.StringWrites.writes("Status")
      JsObject(baseJs.fields :+ ("type" -> typeJs))
    }
  }

  implicit val statusFormat = Format[StatusInfo](Json.reads[StatusInfo], writes)

  def create(applicationMode: play.api.Mode): StatusInfo = new StatusInfo(
    applicationMode = applicationMode,
    buildTime = buildinfo.BuildInfo.buildTime,
    appVersion = buildinfo.BuildInfo.version,
    scalaVersion = buildinfo.BuildInfo.scalaVersion,
    sbtVersion = buildinfo.BuildInfo.sbtVersion,
    scalazVersion = buildinfo.BuildInfo.scalazVersion,
    elasticVersion = buildinfo.BuildInfo.elasticVersion,
    playVersion = buildinfo.BuildInfo.playVersion
  )

}

case class StatusInfo(
                       applicationMode: play.api.Mode,
                       buildTime: String,
                       appVersion: String,
                       scalaVersion: String,
                       sbtVersion: String,
                       scalazVersion: String,
                       elasticVersion: String,
                       playVersion: String
                     )
