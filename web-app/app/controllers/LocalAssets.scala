/*
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package controllers

import java.io._

import play.api.Play.current
import play.api._
import play.api.mvc._

/**
 * Controller that serves static resources from an external folder.
 * It useful in development mode if you want to serve static assets that shouldn't be part of the build process.
 *
 * Not that this controller is not intented to be used in production mode and can lead to security issues.
 * Therefore it is automatically disabled in production mode.
 *
 * All assets are served with max-age=3600 cache directive.
 *
 * You can use this controller in any application, just by declaring the appropriate route. For example:
 * {{{
 * GET     /assets/\uFEFF*file               controllers.ExternalAssets.at(path="/home/peter/myplayapp/external", file)
 * GET     /assets/\uFEFF*file               controllers.ExternalAssets.at(path="C:\external", file)
 * GET     /assets/\uFEFF*file               controllers.ExternalAssets.at(path="relativeToYourApp", file)
 * }}}
 *
 */
object LocalAssets extends Controller {

  val AbsolutePath = """^(/|[a-zA-Z]:\\).*""".r

  /**
   * Generates an `Action` that serves a static resource from an external folder
   *
   * @param absoluteRootPath the root folder for searching the static resource files such as `"/home/peter/public"`, `C:\external` or `relativeToYourApp`
   * @param file the file part extracted from the URL
   */
  def at(rootPath: String, file: String): Action[AnyContent] = Action { request =>

    // ???? Should the routing strip the prefix ????
    val fileName = file.stripPrefix("static/bs")
    //Logger.info(s"path is $rootPath file is $fileName")

    Play.mode match {
      case Mode.Prod => NotFound
      case _ => {

        val fileToServe = rootPath match {
          case AbsolutePath(_) => new File(rootPath, fileName)
          case _ => new File(Play.application.getFile(rootPath), fileName)
        }

        if (fileToServe.exists) {
          Ok.sendFile(fileToServe, inline = true).withHeaders(CACHE_CONTROL -> "max-age=3600")
        } else {
          NotFound
        }

      }
    }
  }

}
