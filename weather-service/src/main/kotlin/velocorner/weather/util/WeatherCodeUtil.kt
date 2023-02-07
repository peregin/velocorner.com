package velocorner.weather.util

import org.slf4j.LoggerFactory

/**
 * Utility to convert the weather code mappings into the model.
 *
 * # Group 5xx: Rain
 * # ID	Meaning	                    Icon BootstrapIcon
 * 500	light rain	                10d icon-weather-008
 * 501	moderate rain	            10d icon-weather-007
 */

data class WeatherCode(val code: Int, val meaning: String, val bootstrapIcon: String)

class WeatherCodeUtil {

    companion object {

        private val logger = LoggerFactory.getLogger(this::class.java)
        private val code2Model by lazy {
            fromResources()
        }

        fun fromResources(): Map<Int, WeatherCode> {
            val url = WeatherCodeUtil::class.java.getResource("/weather_codes.txt")
            val entries = url.readText().lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .filter { !it.startsWith("#") }
                .map { parse(it) }

            // log errors if any
            entries.filter { it.isFailure }
                .forEach { logger.error("failed to parse line ${it.exceptionOrNull()?.message}") }

            return entries.filter { it.isSuccess }.map { it.getOrThrow() }.map { e -> (e.code to e) }.toMap()
        }

        fun parse(line: String): Result<WeatherCode> = runCatching {
            val tokens = line.split("\t").map { it.trim() }
            WeatherCode(
                code = tokens[0].toInt(),
                meaning = tokens[1],
                bootstrapIcon = tokens[2].split(" ")[1]
            )
        }

        fun bootstrapIcon(code: Int): String = code2Model[code]?.bootstrapIcon ?: error("invalid weather code $code")
    }
}