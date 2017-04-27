package velocorner.manual.spark

import org.apache.spark.SparkContext
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.slf4s.Logging
import velocorner.manual.MyMacConfig
import velocorner.model.Activity
import velocorner.spark.LocalSpark
import velocorner.util.{JsonIo, Metrics}


object MachineLearningApp extends App with LocalSpark[String] with Logging with Metrics with MyMacConfig {

  log.info("starting...")

  runSpark()

  override def sparkAppName: String = "Predict Activities"

  override def spark(sc: SparkContext): String = {
    log.info("connecting to a data source...")

    val activities = timed("read json from gzip") {
      JsonIo.readFromGzipResource[List[Activity]]("/data/strava/activities.json.gz")
    }
    log.info(s"got ${activities.size} activities")
    val data2015 = activities.filter(_.start_date.getYear == 2015)
    log.info(s"got ${data2015.size} activities from 2015")

    // prepare training set
    val parsedTrainData = data2015.map(_.labeledPoint)
    val rddTraining = sc.makeRDD(parsedTrainData).cache()
    val scaler = new StandardScaler(withMean = true, withStd = true).fit(rddTraining.map(x => x.features))
    val scaledTrainData = parsedTrainData.map(x => LabeledPoint(x.label, scaler.transform(Vectors.dense(x.features.toArray))))

    val algorithm = new LinearRegression().setMaxIter(10)
    //algorithm.fit(scaledTrainData)
    "done"
  }


  implicit class FeatureExtractor(activity: Activity) {
    // features:
    // - distance - as label or predicted
    // - month
    // - day
    // - day of week - work days vs weekends
    def features: Array[Double] = Array(
      activity.start_date.getMonthOfYear.toDouble,
      activity.start_date.getDayOfMonth.toDouble,
      activity.start_date.getDayOfWeek.toDouble
    )

    def labeledPoint: LabeledPoint = LabeledPoint(activity.distance.toDouble, Vectors.dense(features))
  }
}
