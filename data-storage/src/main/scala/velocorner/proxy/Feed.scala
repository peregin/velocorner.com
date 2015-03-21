package velocorner.proxy

import velocorner.model.Activity


trait Feed {

  def recentClubActivities(clubId: Long): List[Activity]

  def recentAthleteActivities: List[Activity]

  def listAthleteActivities: List[Activity]
}
