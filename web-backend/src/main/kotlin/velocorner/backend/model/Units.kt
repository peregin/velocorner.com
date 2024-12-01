package velocorner.backend.model

enum class Units(
    val speedLabel: String,
    val distanceLabel: String,
    val elevationLabel: String,
    val temperatureLabel: String
) {
    METRIC("km/h", "km", "m", "°C"),
    IMPERIAL("mph", "mi", "ft", "°F");
}