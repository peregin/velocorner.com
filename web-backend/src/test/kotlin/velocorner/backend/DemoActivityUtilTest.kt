package velocorner.backend

import velocorner.backend.util.DemoActivityUtil

import kotlin.test.*
import java.time.LocalDate
import org.junit.jupiter.api.Test

internal class DemoActivityUtilTest {

    @Test
    fun `test generate activities`() {
        // given
        val endDate = LocalDate.of(2024, 1, 1)
        val yearsBack = 2

        // when
        val activities = DemoActivityUtil.generate(endDate, yearsBack).toList()

        // then
        assertNotNull(activities)
        assertTrue(activities.isNotEmpty())

        // verify date range
        val startDate = endDate.minusYears(yearsBack.toLong())
        activities.forEach { activity ->
            assertTrue(activity.startDate.toLocalDate() >= startDate)
            assertTrue(activity.startDate.toLocalDate() <= endDate)
        }

        // verify activity properties
        activities.forEach { activity ->
            assertEquals("Ride", activity.type)
            assertTrue(activity.distance in 3000f..120000f)
            assertTrue(activity.movingTime in 60000..6000000)
            assertTrue(activity.totalElevationGain >= 50f)
            assertEquals(activity.movingTime, activity.elapsedTime)
            assertNotNull(activity.name)
            assertTrue(activity.name.length == 10)
        }
    }

    @Test
    fun `test generate titles`() {
        // given
        val count = 5

        // when
        val titles = DemoActivityUtil.generateTitles(count)

        // then
        assertEquals(count, titles.size)
        assertEquals(titles.toSet().size, titles.size)
    }

    @Test
    fun `test activities are generated in chronological order`() {
        // when
        val activities = DemoActivityUtil.generate().toList()

        // then
        for (i in 1 until activities.size) {
            assertTrue(
                activities[i-1].startDate <= activities[i].startDate,
                "Activities should be in chronological order"
            )
        }
    }

    @Test
    fun `test consecutive activities have 1-4 days gap`() {
        // when
        val activities = DemoActivityUtil.generate().toList()

        // then
        for (i in 1 until activities.size) {
            val daysBetween = java.time.Duration.between(
                activities[i-1].startDate,
                activities[i].startDate
            ).toDays()

            assertTrue(
                daysBetween in 1..4,
                "Gap between activities should be 1-4 days, was $daysBetween"
            )
        }
    }

    @Test
    fun `test demo athlete properties`() {
        // when
        val activities = DemoActivityUtil.generate().take(1).toList()

        // then
        activities.forEach { activity ->
            with(activity.athlete) {
                assertEquals(1L, id)
                assertEquals("Rider", firstname)
                assertEquals("Demo", lastname)
                assertEquals("Zurich", city)
                assertEquals("Switzerland", country)
                assertNull(profileMedium)
                assertNull(bikes)
                assertNull(shoes)
            }
        }
    }
}
