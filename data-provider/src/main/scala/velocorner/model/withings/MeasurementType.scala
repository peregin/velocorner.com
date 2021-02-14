package velocorner.model.withings

/**  Measurement type is one of the following:
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
object MeasurementType {

  val all = Seq(
    Weight,
    Height,
    FatFreeMass,
    FatRatio,
    FatMassWeight,
    DiastolicBloodPressure,
    SystolicBloodPressure,
    HeartPulse,
    Temperature,
    SP02,
    BodyTemperature,
    SkinTemperature,
    MuscleMass,
    Hydration,
    BoneMass,
    PulseWaveVelocity
  )

  val value2Type = all.map(o => (o.value, o)).toMap

  sealed class Entry(val name: String, val value: Int)

  case object Weight extends Entry("Weight", 1)

  case object Height extends Entry("Height", 4)

  case object FatFreeMass extends Entry("FatFreeMass", 5)

  case object FatRatio extends Entry("FatRatio", 6)

  case object FatMassWeight extends Entry("FatMassWeight", 8)

  case object DiastolicBloodPressure extends Entry("DiastolicBloodPressure", 9)

  case object SystolicBloodPressure extends Entry("SystolicBloodPressure", 10)

  case object HeartPulse extends Entry("HeartPulse", 11)

  case object Temperature extends Entry("Temperature", 12)

  case object SP02 extends Entry("SP02", 54)

  case object BodyTemperature extends Entry("BodyTemperature", 71)

  case object SkinTemperature extends Entry("BodyTemperature", 73)

  case object MuscleMass extends Entry("MuscleMass", 76)

  case object Hydration extends Entry("Hydration", 77)

  case object BoneMass extends Entry("BoneMass", 88)

  case object PulseWaveVelocity extends Entry("PulseWaveVelocity", 91)

}
