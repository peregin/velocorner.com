package velocorner.api

object Role {

  sealed abstract class Entry

  case object Admin extends Entry
}
