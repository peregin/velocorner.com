package velocorner.api.wordcloud

import play.api.libs.json.{Format, Json, Reads}

object WordCloud {
  implicit val cloudFormat: Format[WordCloud] = Format[WordCloud](Json.reads[WordCloud], Json.writes[WordCloud])
  implicit val listCloud: Reads[List[WordCloud]] = Reads.list(cloudFormat)
}

case class WordCloud(name: String, weight: Long)
