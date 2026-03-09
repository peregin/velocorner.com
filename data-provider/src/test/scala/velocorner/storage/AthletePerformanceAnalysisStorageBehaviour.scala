package velocorner.storage

import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import velocorner.api.AthletePerformanceAnalysis
import velocorner.manual.AwaitSupport

import scala.concurrent.Future

trait AthletePerformanceAnalysisStorageBehaviour extends Matchers with AwaitSupport { this: AnyFlatSpec =>

  def athletePerformanceAnalysisFragments(storage: => Storage[Future]): Unit = {
    lazy val analysisStorage = storage.getAthletePerformanceAnalysisStorage

    def assertAnalysis(actual: AthletePerformanceAnalysis, expected: AthletePerformanceAnalysis): Unit = {
      actual.copy(createdAt = expected.createdAt) mustBe expected
      actual.createdAt.getMillis mustBe expected.createdAt.getMillis
    }

    val createdAt1 = DateTime.parse("2026-03-01T10:00:00Z").withZone(DateTimeZone.UTC)
    val createdAt2 = DateTime.parse("2026-03-02T10:00:00Z").withZone(DateTimeZone.UTC)
    val createdAt3 = DateTime.parse("2026-03-03T10:00:00Z").withZone(DateTimeZone.UTC)

    val analysis1 = AthletePerformanceAnalysis(
      athleteId = 432909L,
      fingerprint = "fp-1",
      basedOn = "5 latest activities",
      summary = "Summary 1",
      createdAt = createdAt1
    )
    val analysis2 = AthletePerformanceAnalysis(
      athleteId = 432909L,
      fingerprint = "fp-2",
      basedOn = "5 latest activities",
      summary = "Summary 2",
      createdAt = createdAt2
    )

    it should "read empty analysis for unknown athlete/fingerprint" in {
      awaitOn(analysisStorage.latest(-1L)) mustBe empty
      awaitOn(analysisStorage.byFingerprint(432909L, "unknown-fp")) mustBe empty
    }

    it should "store and retrieve an analysis by fingerprint" in {
      awaitOn(analysisStorage.store(analysis1))
      val maybeActual = awaitOn(analysisStorage.byFingerprint(432909L, "fp-1"))
      assertAnalysis(maybeActual.getOrElse(sys.error("analysis not found")), analysis1)
    }

    it should "return the latest analysis for an athlete" in {
      awaitOn(analysisStorage.store(analysis2))
      val maybeActual = awaitOn(analysisStorage.latest(432909L))
      assertAnalysis(maybeActual.getOrElse(sys.error("analysis not found")), analysis2)
    }

    it should "upsert the same fingerprint and keep latest version" in {
      val updated = analysis2.copy(summary = "Updated summary", createdAt = createdAt3)
      awaitOn(analysisStorage.store(updated))
      assertAnalysis(awaitOn(analysisStorage.byFingerprint(432909L, "fp-2")).getOrElse(sys.error("analysis not found")), updated)
      assertAnalysis(awaitOn(analysisStorage.latest(432909L)).getOrElse(sys.error("analysis not found")), updated)
    }

    it should "isolate analyses per athlete" in {
      val athlete2 = AthletePerformanceAnalysis(
        athleteId = 777L,
        fingerprint = "fp-2",
        basedOn = "5 latest activities",
        summary = "Other athlete summary",
        createdAt = createdAt2
      )
      awaitOn(analysisStorage.store(athlete2))

      assertAnalysis(awaitOn(analysisStorage.byFingerprint(777L, "fp-2")).getOrElse(sys.error("analysis not found")), athlete2)
      awaitOn(analysisStorage.byFingerprint(432909L, "fp-2")).map(_.summary) mustBe Some("Updated summary")
    }
  }
}
