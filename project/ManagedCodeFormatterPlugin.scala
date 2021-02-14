import org.scalafmt.sbt.ScalafmtPlugin
import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtConfig
import sbt.Keys._
import sbt._
import sbt.internal.util.ManagedLogger
import sbt.nio.file.FileTreeView

import java.io.OutputStreamWriter
import java.lang.invoke.MethodHandles

object ManagedCodeFormatterPlugin extends AutoPlugin {

  override def requires: Plugins = ScalafmtPlugin

  object autoImport {
    val scalafmtGenerated = taskKey[Unit]("Format generated scala files, e.g. **/src_managed/*.scala files")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    scalafmtGenerated := {
      val log = streams.value.log
      val dirs = (Compile / managedSourceDirectories).value
      val configPath = scalafmtConfig.value.toPath
      log.info(s"Generated sources [$configPath] from ${dirs.mkString("\n\t", "\n\t", "\n")}")
      val filesGlob = dirs.map(d => d.toGlob / ** / "*.scala")
      val files = FileTreeView.default.list(filesGlob).map(_._1.toFile)
      log.info(s"Found ${files.size} generated files")
      invokeObjectPrivateMethod[Unit](
        log,
        "org.scalafmt.sbt",
        "ScalafmtPlugin",
        "formatSources",
        streams.value.cacheStoreFactory,
        files,
        configPath,
        streams.value.log,
        new OutputStreamWriter(streams.value.binary()),
        fullResolvers.value
      )
    }
  )

  // to invoke private method from the ScalafmtPlugin object
  def invokeObjectPrivateMethod[R](log: ManagedLogger, packageName: String, objectName: String, methodName: String, args: AnyRef*): R = {
    val javaClassName = s"$packageName.${objectName.replace('.', '$')}$$"
    val clazz = Class.forName(javaClassName)
    val lookup = MethodHandles.lookup
    val field = clazz.getField("MODULE$")
    val fieldMethodHandle = lookup.unreflectGetter(field)
    val instance = fieldMethodHandle.invokeWithArguments()
    val methods = clazz.getDeclaredMethods.filter(_.getName == methodName)
    log.debug(s"${methods.size} methods with name $methodName")
    val method = methods.find(_.getParameterCount == args.length).get // for method overloading
    log.debug(s"parameters ${method.getParameterTypes.mkString(",")}")
    method.setAccessible(true)
    method.invoke(instance, args: _*).asInstanceOf[R]
  }
}
