package velocorner.weather.service

class Controller(val apiKey: String) {

    fun current(location: String): String = location
}