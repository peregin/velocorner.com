package velocorner.api.wordcloud

import play.api.libs.json.{Format, Json}

object WordCloud {
  implicit val cloudFormat = Format[WordCloud](Json.reads[WordCloud], Json.writes[WordCloud])
}

case class WordCloud(name: String, weight: Long)
