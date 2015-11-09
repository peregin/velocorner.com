package velocorner.proxy

import velocorner.model.Activity


trait Feed {

  // authentication
  //def getOAuth2Url(redirectUrl: String): String

  //def getOAuth2Token(code: String): (accces token, athlete)

  // club
  def recentClubActivities(clubId: Long): List[Activity]

  // athlete
  def recentAthleteActivities: List[Activity]

  def listAthleteActivities: List[Activity]
}
