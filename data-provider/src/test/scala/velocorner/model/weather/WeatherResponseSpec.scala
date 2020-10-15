package velocorner.model.weather

import org.joda.time.{DateTime, DateTimeZone}
import org.specs2.mutable.Specification
import velocorner.util.JsonIo

class WeatherResponseSpec extends Specification {

  "weather response" should {

    val response =
      """{
        |  "coord": {
        |    "lon": 8.52,
        |    "lat": 47.31
        |  },
        |  "weather": [
        |    {
        |      "id": 801,
        |      "main": "Clouds",
        |      "description": "few clouds",
        |      "icon": "02d"
        |    }
        |  ],
        |  "base": "stations",
        |  "main": {
        |    "temp": 7.57,
        |    "feels_like": 4.53,
        |    "temp_min": 5,
        |    "temp_max": 10,
        |    "pressure": 1012,
        |    "humidity": 81
        |  },
        |  "visibility": 10000,
        |  "wind": {
        |    "speed": 2.6,
        |    "deg": 20
        |  },
        |  "clouds": {
        |    "all": 20
        |  },
        |  "dt": 1602670919,
        |  "sys": {
        |    "type": 1,
        |    "id": 6932,
        |    "country": "CH",
        |    "sunrise": 1602654185,
        |    "sunset": 1602693616
        |  },
        |  "timezone": 7200,
        |  "id": 2661861,
        |  "name": "Adliswil",
        |  "cod": 200
        |}""".stripMargin

    "be parsed into model" in {
      val model = JsonIo.read[WeatherResponse](response)
      model.cod === 200
      model.sys.map(_.sunrise.dayOfYear()) should beSome(DateTime
        .parse("2020-10-14T07:43:05.000+02:00")
        .withZone(DateTimeZone.forID("Europe/Zurich"))
        .dayOfYear()
      )
      model.coord.map(_.lon) should beSome(8.52d)
      model.coord.map(_.lat) should beSome(47.31d)
    }
  }
}
