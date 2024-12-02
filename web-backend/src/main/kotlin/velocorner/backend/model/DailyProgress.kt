package velocorner.backend.model

import java.time.LocalDate

data class DailyProgress(val day: LocalDate, val progress: Progress) {

    companion object {
        fun from(activity: Activity): DailyProgress {
            val progress = Progress(
                days = 1,
                rides = 1,
                distance = activity.distance / 1000,
                longestDistance = activity.distance / 1000,
                movingTime = activity.moving_time.toLong(),
                averageSpeed = activity.average_speed?.toDouble() ?: 0.0,
                elevation = activity.total_elevation_gain,
                longestElevation = activity.total_elevation_gain
            )
            return DailyProgress(activity.getStartDateLocal().toLocalDate(), progress)
        }

        fun from(activities: Sequence<Activity>): List<DailyProgress> =
            activities
                .map { from(it) }
                .groupBy { it.day }
                .map { (day, progressPerDay) ->
                    DailyProgress(
                        day = day,
                        progress = progressPerDay
                            .fold(Progress.zero) { accu, dailyProgress ->
                                accu + dailyProgress.progress
                            }
                            .copy(days = 1)
                    )
                }
                .sortedBy { it.day.toString() }

        fun aggregate(list: Iterable<DailyProgress>): List<DailyProgress> =
            list.scan(DailyProgress(LocalDate.now(), Progress.zero)) { accu, item ->
                DailyProgress(item.day, accu.progress + item.progress)
            }.drop(1)
    }
}

