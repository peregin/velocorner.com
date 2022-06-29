package velocorner.model.brand

object NameNormalizer {

  type Transform = String => String
  val nameNormalizer = List[Transform](
    _.filterNot(_.isWhitespace),
    _.toLowerCase,
    _.replaceAll("[^a-z\\d/]", ""),
    s => Option(s).filter(_.length > 5).map(_.stripSuffix("s")).getOrElse(s)
  ).reduce(_ andThen _)

  implicit class NameNormalizerOps(val name: String) extends AnyVal {
    def normalize(): String = nameNormalizer(name)
  }
}
