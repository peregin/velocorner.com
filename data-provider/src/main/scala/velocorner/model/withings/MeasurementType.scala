package velocorner.model.withings

import enumeratum._

sealed abstract class MeasurementType(val name: String, val value: Int) extends EnumEntry

/**
 *  Measurement type is one of the following:
 *
 *  1 : Weight (kg)
 *  4 : Height (meter)
 *  5 : Fat Free Mass (kg)
 *  6 : Fat Ratio (%)
 *  8 : Fat Mass Weight (kg)
 *  9 : Diastolic Blood Pressure (mmHg)
 *  10 : Systolic Blood Pressure (mmHg)
 *  11 : Heart Pulse (bpm)
 *  12 : Temperature
 *  54 : SP02(%)
 *  71 : Body Temperature
 *  73 : Skin Temperature
 *  76 : Muscle Mass
 *  77 : Hydration
 *  88 : Bone Mass
 *  91 : Pulse Wave Velocity
 */
object MeasurementType extends Enum[MeasurementType] {

  val values = findValues

  val value2Type = values.map(o => (o.value, o)).toMap

  case object Weight extends MeasurementType("Weight", 1)

  case object Height extends MeasurementType("Height", 4)

  case object FatFreeMass extends MeasurementType("FatFreeMass", 5)

  case object FatRatio extends MeasurementType("FatRatio", 6)

  case object FatMassWeight extends MeasurementType("FatMassWeight", 8)

  case object DiastolicBloodPressure extends MeasurementType("DiastolicBloodPressure", 9)

  case object SystolicBloodPressure extends MeasurementType("SystolicBloodPressure", 10)

  case object HeartPulse extends MeasurementType("HeartPulse", 11)

  case object Temperature extends MeasurementType("Temperature", 12)

  case object SP02 extends MeasurementType("SP02", 54)

  case object BodyTemperature extends MeasurementType("BodyTemperature", 71)

  case object SkinTemperature extends MeasurementType("BodyTemperature", 73)

  case object MuscleMass extends MeasurementType("MuscleMass", 76)

  case object Hydration extends MeasurementType("Hydration", 77)

  case object BoneMass extends MeasurementType("BoneMass", 88)

  case object PulseWaveVelocity extends MeasurementType("PulseWaveVelocity", 91)

}
