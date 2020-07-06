package velocorner.model

object Role {

  sealed abstract class Entry

  case object Admin extends Entry
}