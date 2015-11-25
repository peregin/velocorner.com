package velocorner.proxy

import velocorner.model.{Authentication, Activity}


trait Feed {

  // authentication
  def getOAuth2Url(redirectHost: String): String

  def getOAuth2Token(code: String, clientSecret: String): Authentication


  // club
  def recentClubActivities(clubId: Long): List[Activity]


  // athlete
  def recentAthleteActivities: List[Activity]

  def listAthleteActivities: List[Activity]
}
