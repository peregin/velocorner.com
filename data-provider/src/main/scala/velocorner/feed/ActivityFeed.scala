package velocorner.feed

import velocorner.api.{Activity, Athlete}

import scala.concurrent.Future


trait ActivityFeed {

  // club
  def listRecentClubActivities(clubId: Long): Future[List[Activity]]

  def listClubAthletes(clubId: Long): Future[List[Athlete]]

  // activities - page starts from 1 and pageSize cannot be greater than StravaActivityFeed.maxItemsPerPage
  def listAthleteActivities(page: Int, pageSize: Int): Future[List[Activity]]

  // athlete, of the authenticated user
  def getAthlete: Future[Athlete]

}
