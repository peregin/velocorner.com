package velocorner.model

/**
  * Created by levi on 05/12/15.
  */
sealed trait Permission
case object Admin extends Permission
case object Normal extends Permission
