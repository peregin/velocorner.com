package velocorner.util

import com.sksamuel.elastic4s.indexes.IndexDefinition
import velocorner.model.Activity

/**
  * Created by levi on 21.03.17.
  */
trait ElasticSupport {

  def extractIndices(a: Activity, id: IndexDefinition): IndexDefinition = id.fields(
    "name" -> a.name,
    "start_date" -> a.start_date,
    "distance" -> a.distance / 1000,
    "elevation" -> a.total_elevation_gain,
    "average_speed" -> a.average_speed.getOrElse(0f),
    "max_speed" -> a.max_speed.getOrElse(0f),
    "average_temp" -> a.average_temp.getOrElse(0f),
    "average_watts" -> a.average_watts.getOrElse(0f)
  )
}
