package velocorner.proxy

import velocorner.model.{Athlete, Authentication, Activity}


trait Feed {

  // authentication
  def getOAuth2Url(redirectHost: String): String

  def getOAuth2Token(code: String, clientSecret: String): Authentication


  // club
  def recentClubActivities(clubId: Long): List[Activity]


  // activities
  def recentAthleteActivities: List[Activity]

  def listAthleteActivities: List[Activity]

  // athlete
  def getAthlete: Athlete
}
