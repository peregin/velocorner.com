package velocorner.model

import org.joda.time.DateTime
import velocorner.model.MeasurementType.Entry

// Extracted from the Withings feed and persisted in the storage layer.
// Represents a generic measurement entry.
case class Measurement(
                        id: String,
                        `type`: Entry,
                        value: Double,
                        athleteId: Int,
                        updateTime: DateTime
)