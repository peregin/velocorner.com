package velocorner.storage

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx
import com.orientechnologies.orient.core.metadata.schema.{OClass, OType}
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import org.slf4s.Logging
import velocorner.model._
import velocorner.storage.OrientDbStorage._
import velocorner.util.JsonIo

import collection.JavaConverters._
import scala.language.implicitConversions

/**
  * Created by levi on 14.11.16.
  */
class OrientDbStorage extends Storage with Logging {

  @volatile var db: Option[ODatabaseDocumentTx] = None

  // insert all activities, new ones are added, previous ones are overridden
  override def store(activities: Iterable[Activity]) = inTx {
    activities.foreach{ a =>
      // upsert
      val sql = s"SELECT FROM $ACTIVITY_CLASS WHERE id = ${a.id}"
      val results: java.util.List[ODocument] = db.query(new OSQLSynchQuery[ODocument](sql))
      val doc = results.asScala.headOption.getOrElse(new ODocument(ACTIVITY_CLASS))
      doc.fromJSON(JsonIo.write(a))
      doc.save()
    }
  }

  override def dailyProgressForAthlete(athleteId: Int): Iterable[DailyProgress] = {
    val activities = activitiesFor(s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = 'Ride'")
    log.debug(s"found activities ${activities.size} for $athleteId")
    DailyProgress.fromStorage(activities)
  }

  override def dailyProgressForAll(limit: Int): Iterable[AthleteDailyProgress] = {
    val activities = activitiesFor(s"SELECT FROM $ACTIVITY_CLASS WHERE type = 'Ride' ORDER BY start_date DESC LIMIT $limit")
    log.debug(s"found activities ${activities.size}")
    AthleteDailyProgress.fromStorage(activities).toList.sortBy(_.dailyProgress.day.toString)
  }

  // summary on the landing page
  override def listRecentActivities(limit: Int): Iterable[Activity] = {
    activitiesFor(s"SELECT FROM $ACTIVITY_CLASS WHERE type = 'Ride' ORDER BY start_date DESC LIMIT $limit")
  }

  // to check how much needs to be imported from the feed
  override def listRecentActivities(athleteId: Int, limit: Int): Iterable[Activity] = {
    activitiesFor(s"SELECT FROM $ACTIVITY_CLASS WHERE athlete.id = $athleteId AND type = 'Ride' ORDER BY start_date DESC LIMIT $limit")
  }

  private def activitiesFor(sql: String): Seq[Activity] = inTx {
    val results: java.util.List[ODocument] = db.query(new OSQLSynchQuery[ODocument](sql))
    results.asScala.map(d => JsonIo.read[Activity](d.toJSON))
  }

  private def upsert(json: String, className: String, propertyName: String, propertyValue: Int) = inTx {
    val sql = s"SELECT FROM $className WHERE $propertyName = $propertyValue"
    val results: java.util.List[ODocument] = db.query(new OSQLSynchQuery[ODocument](sql))
    val doc = results.asScala.headOption.getOrElse(new ODocument(className))
    doc.fromJSON(json)
    doc.save()
  }

  private def query(className: String, propertyName: String, propertyValue: Int): Option[String] = {
    val sql = s"SELECT FROM $className WHERE $propertyName = $propertyValue"
    val results: java.util.List[ODocument] = db.query(new OSQLSynchQuery[ODocument](sql))
    results.asScala.headOption.map(_.toJSON)
  }

  // accounts
  override def store(account: Account) {
    upsert(JsonIo.write(account), ACTIVITY_CLASS, "athleteId", account.athleteId)
  }

  override def getAccount(id: Long): Option[Account] = query(ACCOUNT_CLASS, "athleteId", id.toInt).map(JsonIo.read[Account])

  // athletes
  override def store(athlete: Athlete) {
    upsert(JsonIo.write(athlete), ATHLETE_CLASS, "id", athlete.id)
  }

  override def getAthlete(id: Long): Option[Athlete] = query(ATHLETE_CLASS, "id", id.toInt).map(JsonIo.read[Athlete])

  // clubs
  override def store(club: Club) {
    upsert(JsonIo.write(club), CLUB_CLASS, "id", club.id)
  }

  override def getClub(id: Long): Option[Club] = query(CLUB_CLASS, "id", id.toInt).map(JsonIo.read[Club])

  // initializes any connections, pools, resources needed to open a storage session
  override def initialize() {
    // TODO: extract to config
    val rootDir = "orientdb_data"

    val odb = new ODatabaseDocumentTx(s"plocal:$rootDir/velocorner")
    if (!odb.exists()) {
      odb.create()
      odb.close()
    }
    db = Some(odb)

    inTx {
      def createIfNeeded(className: String, indexName: String, indexType: OType) {
        val schema = odb.getMetadata.getSchema
        if (!schema.existsClass(className)) schema.createClass(className)
        val clazz = schema.getClass(className)
        if (!clazz.existsProperty(indexName)) clazz.createProperty(indexName, indexType)
        if (!clazz.areIndexed(indexName)) clazz.createIndex(s"$indexName$className", OClass.INDEX_TYPE.UNIQUE, indexName)
      }
      createIfNeeded(ACTIVITY_CLASS, "id", OType.INTEGER)
      createIfNeeded(ACCOUNT_CLASS, "athleteId", OType.INTEGER)
      createIfNeeded(CLUB_CLASS, "id", OType.INTEGER)
      createIfNeeded(ATHLETE_CLASS, "id", OType.INTEGER)
    }
  }

  // releases any connections, resources used
  override def destroy() {}

  def inTx[T](body: => T): T = {
    db.open("admin", "admin")
    import scala.util.control.Exception._
    ultimately(db.close()).apply(body)
  }
}

object OrientDbStorage {

  val ACTIVITY_CLASS = "Activity"
  val ACCOUNT_CLASS = "Account"
  val CLUB_CLASS = "Club"
  val ATHLETE_CLASS = "Athlete"

  implicit def dbOrFail(db: Option[ODatabaseDocumentTx]): ODatabaseDocumentTx = db.getOrElse(sys.error("db is not initialized"))
}
