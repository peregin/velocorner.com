package velocorner.spark.app

import org.apache.spark.SparkContext
import org.apache.spark.internal.Logging
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.SparkSession
import org.joda.time.DateTime
import velocorner.spark.LocalSpark
import velocorner.spark.app.fixit.Activity

object PredictActivities extends App with LocalSpark[String] with Logging {

  log.info("starting...")

  runSpark()

  override def sparkAppName: String = "Predict Activities"

  override def spark(sc: SparkContext): String = {
    log.info("connecting to a data source...")

    val activities = List.empty[Activity]
      //timed("read json from gzip") {
      //JsonIo.readFromGzipResource[List[Activity]]("/data/strava/activities.json.gz")
    //}
    log.info(s"got ${activities.size} activities")
    val data2015 = activities.filter(_.start_date.getYear == 2015)
    log.info(s"got ${data2015.size} activities from 2015")

    // prepare training set
    val parsedData = data2015.map(_.labeledPoint)
    val rdd = sc.makeRDD(parsedData).cache()

    val algorithm = new LinearRegression().setMaxIter(10).setRegParam(0.3).setElasticNetParam(0.8)
    val session = SparkSession.builder().getOrCreate()
    val Array(trainingData, testData) = session.createDataFrame(rdd).randomSplit(Array(0.7, 0.3), 1234)

    val model = algorithm.fit(trainingData)

    log.info(s"coefficients: ${model.coefficients} intercept: ${model.intercept}")
    model.summary.residuals.show()

    val predictions = model.transform(testData)
    predictions.show()
    predictions.toString()
  }


  implicit class FeatureExtractor(activity: Activity) {
    // features:
    // - distance - as label or predicted
    // - month
    // - day
    // - day of week - work days vs weekends
    def features: Array[Double] = FeatureExtractor.from(activity.start_date)

    def labeledPoint: LabeledPoint = LabeledPoint(activity.distance.toDouble, Vectors.dense(features))
  }

  object FeatureExtractor {

    def from(startDate: DateTime): Array[Double] = {
      Array(
        startDate.getMonthOfYear.toDouble,
        startDate.getDayOfMonth.toDouble,
        startDate.getDayOfWeek.toDouble
      )
    }
  }
}
