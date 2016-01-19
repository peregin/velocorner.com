package velocorner.proxy

import velocorner.model.{Athlete, Authentication, Activity}


trait Feed {

  // authentication
  def getOAuth2Url(redirectHost: String): String

  def getOAuth2Token(code: String, clientSecret: String): Authentication


  // club
  def listRecentClubActivities(clubId: Long): List[Activity]


  // // activities - page starts from 1 and pageSize cannot be greater than StravaFeed.maxItemsPerPage
  def listAthleteActivities(page: Int, pageSize: Int): List[Activity]

  // athlete
  def getAthlete: Athlete
}
