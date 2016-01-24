package velocorner.proxy

import velocorner.model.{Activity, Athlete}


trait Feed {

  // club
  def listRecentClubActivities(clubId: Long): List[Activity]


  // // activities - page starts from 1 and pageSize cannot be greater than StravaFeed.maxItemsPerPage
  def listAthleteActivities(page: Int, pageSize: Int): List[Activity]

  // athlete
  def getAthlete: Athlete
}
