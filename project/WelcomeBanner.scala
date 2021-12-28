import sbt.Def
import sbt.Keys.version

object WelcomeBanner {

  def text(): Def.Initialize[String] = Def.setting {
    import scala.Console._
    def red(text: String): String = s"$RED$text$RESET"
    def item(text: String): String = s"$GREEN▶ $CYAN$text$RESET"

    s"""|${red("""                                         """)}
        |${red("""          _                              """)}
        |${red(""" __ _____| |___  __ ___ _ _ _ _  ___ _ _ """)}
        |${red(""" \ V / -_) / _ \/ _/ _ \ '_| ' \/ -_) '_|""")}
        |${red("""  \_/\___|_\___/\__\___/_| |_||_\___|_|  """)}
        |${red("""                                         """ + version.value)}
        |
        |Useful sbt tasks:
        |${item("\"project web-app\" run")} - run web application
        |${item("scalafmtGenerated")} - formats generated scala sources
        |${item("fmt")} - command alias to format all files
      """.stripMargin
  }
}
