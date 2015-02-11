package controllers

import play.api.mvc._

/**
 * Controller that serves the static bootstrap resources from an external folder in development mode.
 * The bootstrap library is more 160Mb and it is excluded from the distribution.
 *
 * In dev mode point to a local and external folder.
 *
 * In prod mode the apache reverse proxy maps the configured binding (e.g. /static/bs) to a folder and the rest of the
 * requests are routed to the play environment.
 */
object LocalBootstrapAssets extends Controller {

  /**
   * Generates an `Action` that serves a static resource from an external folder
   *
   * @param rootPath the root folder for searching the static resource files such as `"/static/bs"` see routes definition file
   * @param file the file part extracted from the URL
   */
  def at(rootPath: String, file: String): Action[AnyContent] = {

    val fileName = file.stripPrefix("static/bs") // extract to config or from routes
    ExternalAssets.at(rootPath, fileName)
  }
}
