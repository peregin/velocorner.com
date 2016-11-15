package velocorner.storage

import com.orientechnologies.orient.server.OServer
import org.slf4s.Logging
import velocorner.model._

/**
  * Created by levi on 14.11.16.
  */
class OrientDbStorage extends Storage with Logging {

  // insert all activities, new ones are added, previous ones are overridden
  override def store(activities: Iterable[Activity]): Unit = ???

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

    val config =
      """
        |<orient-server>
        |    <handlers />
        |    <network>
        |        <protocols>
        |            <protocol name="http" implementation="com.orientechnologies.orient.server.network.protocol.http.ONetworkProtocolHttpDb"/>
        |        </protocols>
        |        <listeners>
        |            <listener ip-address="127.0.0.1" port-range="2480" protocol="http">
        |                <commands>
        |                    <command
        |                        pattern="GET|www GET|studio/ GET| GET|*.htm GET|*.html GET|*.xml GET|*.jpeg GET|*.jpg GET|*.png GET|*.gif GET|*.js GET|*.css GET|*.swf GET|*.ico GET|*.txt"
        |                        implementation="com.orientechnologies.orient.server.network.protocol.http.command.get.OServerCommandGetStaticContent">
        |                        <parameters>
        |                            <entry name="http.cache:*.htm *.html" value="Cache-Control: no-cache, no-store, max-age=0, must-revalidate\r\nPragma: no-cache" />
        |                            <entry name="http.cache:default" value="Cache-Control: max-age=120" />
        |                        </parameters>
        |                    </command>
        |                </commands>
        |            </listener>
        |        </listeners>
        |    </network>
        |    <storages>
        |        <storage name="temp" path="memory:temp" userName="admin" userPassword="admin" loaded-at-startup="true"/>
        |    </storages>
        |    <users>
        |        <user name="root" password="root" resources="*"/>
        |        <user name="admin" password="admin" resources="*"/>
        |    </users>
        |    <properties>
        |        <entry name="log.console.level" value="info"/>
        |    </properties>
        |</orient-server>
      """.stripMargin

    val server = new OServer
    server.startup(config).activate()
  }

  // releases any connections, resources used
  override def destroy(): Unit = ???
}
