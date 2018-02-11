package velocorner.model

class MeasurementType(val name: String, val value: Int)

case object Weight extends MeasurementType("Weight", 1)
