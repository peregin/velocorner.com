package velocorner.model

object Role {

  sealed class Entry

  case object Admin extends Entry
}