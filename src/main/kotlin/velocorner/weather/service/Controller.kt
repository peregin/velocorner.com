package velocorner.weather.service

class Controller(val apiKey: String) {

    fun current(): String = "test " + apiKey.takeLast(4)
}