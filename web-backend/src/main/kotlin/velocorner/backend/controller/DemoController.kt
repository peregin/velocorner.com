package velocorner.backend.controller

import org.slf4j.LoggerFactory

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import velocorner.backend.model.*
import velocorner.backend.util.ActivityOps.toSumYtdSeries
import velocorner.backend.util.ActivityOps.toYearlySeries
import velocorner.backend.util.DemoActivityUtil
import java.time.LocalDate

@RestController
@RequestMapping("/api/demo")
class DemoController {

    @GetMapping("/statistics/ytd/{action}/{activity}")
    fun ytdStatistics(
        @PathVariable action: String,
        @PathVariable activity: String
    ): ResponseEntity<Map<String, Any>> {
        val activities = DemoActivityUtil.generate()
        val now = LocalDate.now()
        val series = toSumYtdSeries(activities, now, action, Units.METRIC)

        return ResponseEntity.ok(
            mapOf(
                "status" to "OK",
                "series" to series
            )
        )
    }

    @GetMapping("/statistics/yearly/{action}/{activity}")
    fun yearlyStatistics(
        @PathVariable action: String,
        @PathVariable activity: String
    ): ResponseEntity<Map<String, Any>> {
        val activities = DemoActivityUtil.generate()
        val series = toYearlySeries(activities, action, Units.METRIC)

        return ResponseEntity.ok(
            mapOf(
                "status" to "OK",
                "series" to series
            )
        )
    }

    @GetMapping("/statistics/daily/{action}")
    fun dailyStatistics(
        @PathVariable action: String
    ): ResponseEntity<List<DailyPoint>> {
        val activities = DemoActivityUtil.generate()
        val dailyProgress = DailyProgress.from(activities)

        val series = when (action.lowercase()) {
            "distance" -> dailyProgress.map { dp ->
                DailyPoint(dp.day, dp.progress.to(Units.METRIC).distance)
            }

            "elevation" -> dailyProgress.map { dp ->
                DailyPoint(dp.day, dp.progress.to(Units.METRIC).elevation)
            }

            else -> throw IllegalArgumentException("Not supported action: $action")
        }

        return ResponseEntity.ok(series)
    }

    @GetMapping("/wordcloud")
    fun wordcloud(): ResponseEntity<List<WordCloud>> {
        val wordClouds = DemoActivityUtil
            .generateTitles(200)
            .map { it.trim().lowercase() }
            .groupingBy { it }
            .eachCount()
            .map { (word, count) -> WordCloud(word, count) }
            .filter { it.weight > 1 }
            .sortedByDescending { it.weight }

        return ResponseEntity.ok(wordClouds)
    }

    @GetMapping("/statistics/histogram/{action}/{activity}")
    fun yearlyHistogram(
        @PathVariable action: String,
        @PathVariable activity: String
    ): ResponseEntity<Any> {
        val activities = DemoActivityUtil.generate()
        val series = when (action.lowercase()) {
            "distance" -> Apexcharts.toDistanceHeatmap(activities, activity, Units.METRIC)
            "elevation" -> Apexcharts.toElevationHeatmap(activities, Units.METRIC)
            else -> throw IllegalArgumentException("Not supported action: $action")
        }

        return ResponseEntity.ok(series)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DemoController::class.java)
    }
}
