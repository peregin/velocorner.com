import java.io.OutputStreamWriter
import java.nio.file.Path

import org.scalafmt.interfaces.{ScalafmtSession, ScalafmtSessionFactory}
import org.scalafmt.sbt.ScalafmtPlugin.globalInstance
import org.scalafmt.sbt.ScalafmtSbtReporter
import sbt.librarymanagement.MavenRepository
import sbt.util.Logger

val scalafmtGenerated = taskKey[Unit](
  "Format generated scala files, e.g. **/src_managed/*.scala files"
)

def formatSources(
                   sources: Seq[File],
                   config: Path,
                   log: Logger,
                   writer: OutputStreamWriter,
                   resolvers: Seq[Resolver]
                 ): Unit = {
  val cnt =
    withFormattedSources(sources, config, log, writer, resolvers)(
      (file, input, output) => {
        if (input != output) {
          IO.write(file, output)
          1
        } else {
          0
        }
      }
    ).flatten.sum

  if (cnt > 1) {
    log.info(s"Reformatted $cnt Scala sources")
  }

}

def withFormattedSources[T](
                             sources: Seq[File],
                             config: Path,
                             log: Logger,
                             writer: OutputStreamWriter,
                             resolvers: Seq[Resolver]
                           )(
                             onFormat: (File, String, String) => T
                           ): Seq[Option[T]] = {
  val reporter = new ScalafmtSbtReporter(log, writer)
  val repositories = resolvers.collect {
    case r: MavenRepository => r.root
  }
  val scalafmtSession =
    globalInstance
      .withReporter(reporter)
      .withMavenRepositories(repositories: _*)
      .withRespectVersion(false)
      .withRespectProjectFilters(true) match {
      case t: ScalafmtSessionFactory =>
        val session = t.createSession(config.toAbsolutePath)
        if (session == null) {
          throw new MessageOnlyException(
            "failed to create formatting session. Please report bug to https://github.com/scalameta/sbt-scalafmt"
          )
        }
        session
      case instance =>
        new ScalafmtSession {
          override def format(file: Path, code: String) = instance.format(config, file, code)

          override def matchesProjectFilters(path: Path) = true
        }
    }

  log.debug(
    s"Adding repositories ${repositories.mkString("[", ",", "]")}"
  )
  sources
    .map { file =>
      val input = IO.read(file)
      val output =
        scalafmtSession.format(
          file.toPath.toAbsolutePath,
          input
        )
      Some(onFormat(file, input, output))
    }
}

scalafmtGenerated := {
  val log = streams.value.log
  val files = (((Keys.managedSourceDirectories in Global).value) ** "*.scala").get
  log.info(s"Start formatting managed sources ${files.mkString("\n")}")
  formatSources(
    files,
    scalafmtConfig.map { f =>
      if (f.exists()) {
        f.toPath
      } else {
        throw new MessageOnlyException(s"File not exists: ${f.toPath}")
      }
    }.value,
    streams.value.log,
    new OutputStreamWriter(streams.value.binary()),
    fullResolvers.value
  )
}