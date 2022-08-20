package velocorner.model.withings

import org.joda.time.DateTime

// Extracted from the Withings feed and persisted in the storage layer.
// Represents a generic measurement entry.
case class Measurement(
    id: String,
    `type`: MeasurementType,
    value: Double,
    athleteId: Int,
    updateTime: DateTime
)
