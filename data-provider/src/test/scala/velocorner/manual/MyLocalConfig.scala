package velocorner.manual

trait MyLocalConfig {

  // the property file having the application secrets, strava token, bucket password, etc.
  val home: String = sys.props.get("user.home").getOrElse("/Users/levi")
  sys.props += "config.file" -> s"$home/Downloads/velo/velocorner/local.conf"
}
