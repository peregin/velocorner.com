package velocorner

import velocorner.model.{YearlyProgress, DailyProgress}
import velocorner.proxy.Feed
import velocorner.storage.Storage

/**
 * Stands for the data flow controller.
 * Controls the access to the storage layer (Couchbase) and remote service (Strava).
 * Also throttles the requests to Strava, since the API usage is limited on a per-application basis using a short term,
 * 15 minute, limit and a long term, daily, limit.
 * The default rate limit allows 600 requests every 15 minutes, with up to 30,000 requests per day
 */
class DataHandler(feed: Feed, val repo: Storage) {

  def dailyProgress: List[DailyProgress] = repo.dailyProgress

  def yearlyProgress: List[YearlyProgress] = YearlyProgress.from(dailyProgress)
}
