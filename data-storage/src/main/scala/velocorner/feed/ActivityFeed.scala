package velocorner.feed

import velocorner.model.{Activity, Athlete}


trait ActivityFeed {

  // club
  def listRecentClubActivities(clubId: Long): List[Activity]

  def listClubAthletes(clubId: Long): List[Athlete]

  // activities - page starts from 1 and pageSize cannot be greater than StravaFeed.maxItemsPerPage
  def listAthleteActivities(page: Int, pageSize: Int): List[Activity]

  // athlete
  def getAthlete: Athlete
}
