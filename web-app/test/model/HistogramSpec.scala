package model

import org.specs2.mutable.Specification
import velocorner.api.heatmap.HeatmapPoint

class HistogramSpec extends Specification {

  "histogram" should {

    val ranges = List(
      HeatmapPoint("10km", 10),
      HeatmapPoint("50km", 50),
      HeatmapPoint("100km", 100),
      HeatmapPoint("150km", 150),
      HeatmapPoint("200km", 200)
    )

    "calculate distribution" in {
      val entries = Map(
        2018 -> List(5L, 8L, 80L),
        2020 -> List(25L, 22L, 180L, 210L)
      )
      val histogram = apexcharts.toYearlyHeatmap(entries, ranges)
      histogram must have size 2
      val first = histogram(0)
      first.name === "2020"
      first.data should containTheSameElementsAs(List(
        HeatmapPoint("10km", 0),
        HeatmapPoint("50km", 2),
        HeatmapPoint("100km", 0),
        HeatmapPoint("150km", 0),
        HeatmapPoint("200km", 2)
      ))
      val second = histogram(1)
      second.name === "2018"
      second.data should containTheSameElementsAs(List(
        HeatmapPoint("10km", 2),
        HeatmapPoint("50km", 0),
        HeatmapPoint("100km", 1),
        HeatmapPoint("150km", 0),
        HeatmapPoint("200km", 0)
      ))
    }
  }
}
