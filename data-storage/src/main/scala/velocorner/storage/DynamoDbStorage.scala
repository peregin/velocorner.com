package velocorner.storage

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.{DynamoDB, Item}
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer
import org.slf4s.Logging
import velocorner.model._
import velocorner.storage.DynamoDbStorage._
import velocorner.util.JsonIo

/**
  * Created by levi on 23.10.16.
  */
class DynamoDbStorage extends Storage with Logging {

  var maybeServer: Option[DynamoDBProxyServer] = None

  // insert all activities, new ones are added, previous ones are overridden
  override def store(activities: Iterable[Activity]) {
    val client = new AmazonDynamoDBClient(new BasicAWSCredentials("", ""))
    client.setEndpoint("http://localhost:8000")
    client.setSignerRegionOverride("local")

    val db = new DynamoDB(client)
    val table = db.getTable(ACTIVITY_TABLE)

    activities.foreach{a =>
      val item = new Item().withPrimaryKey("id", a.id).withJSON("doc", JsonIo.write(a))
      table.putItem(item)
    }
  }

  override def dailyProgressForAthlete(athleteId: Int): Iterable[DailyProgress] = ???

  override def dailyProgressForAll(limit: Int): Iterable[AthleteDailyProgress] = ???

  // summary on the landing page
  override def listRecentActivities(limit: Int): Iterable[Activity] = ???

  // to check how much needs to be imported from the feed
  override def listRecentActivities(athleteId: Int, limit: Int): Iterable[Activity] = ???

  // accounts
  override def store(account: Account): Unit = ???

  override def getAccount(id: Long): Option[Account] = ???

  // athletes
  override def store(athlete: Athlete): Unit = ???

  override def getAthlete(id: Long): Option[Athlete] = ???

  // clubs
  override def store(club: Club): Unit = ???

  override def getClub(id: Long): Option[Club] = ???

  // initializes any connections, pools, resources needed to open a storage session
  override def initialize() {
    maybeServer = Some(ServerRunner.createServerFromCommandLineArgs(Array("-inMemory")))
    maybeServer.foreach(_.start())
  }

  // releases any connections, resources used
  override def destroy() {
    maybeServer.foreach(_.stop())
  }

}

object DynamoDbStorage {

  val ACTIVITY_TABLE = "activity"
}
