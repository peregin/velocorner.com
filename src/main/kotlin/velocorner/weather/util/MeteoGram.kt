package velocorner.weather.util

import velocorner.weather.model.ForecastWeather
import java.time.format.DateTimeFormatter

fun toMeteoGramXml(items: List<ForecastWeather>): String {
    val location = items.firstOrNull()?.location ?: "n/a"
    val lastComma = location.lastIndexOf(',')
    val (city, country) = if (lastComma == -1) {
        Pair(location, "n/a")
    } else {
        Pair(location.substring(0, lastComma).trim(), location.substring(lastComma + 1).trim())
    }

    return """
        <weatherdata>
          <location>
            <name>$city</name>
            <country>$country</country>
          </location>
          <credit>
            <link text="Weather forecast" url="https://velocorner.com"/>
          </credit>
          <forecast>
            <tabular>
              ${items.sortedBy { it.timestamp }.map { it.toXml() }.joinToString(separator = "\n")}
            </tabular>
          </forecast>
        </weatherdata>
    """.trimIndent()
}

val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

fun ForecastWeather.toXml(): String {
    val from = this.forecast.dt.format(formatter)
    val to = this.forecast.dt.plusHours(3).format(formatter)
    val precipitation = this.forecast.rain?.`3h` ?: this.forecast.snow?.`3h` ?: 0.0
    return """
        <time from="$from" to="$to">
          <symbol name="${this.forecast.weather.first().description}" var="${this.forecast.weather.first().icon}"/>
          <precipitation value="${precipitation.to1DecimalPlace()}"/>
          <windDirection deg="${this.forecast.wind.deg.toInt()}"/>
          <windSpeed mps="${(this.forecast.wind.speed * 3.6).to1DecimalPlace()}"/>
          <temperature unit="celsius" value="${this.forecast.main.temp.toDouble().to1DecimalPlace()}"/>
          <pressure unit="hPa" value="${this.forecast.main.pressure.toDouble().to1DecimalPlace()}"/>
        </time>
    """.trimIndent()
}

fun Double.to1DecimalPlace(): String = "%.1f".format(this)