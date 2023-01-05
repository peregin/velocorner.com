package velocorner.feed

import cats.data.{NonEmptyList, Validated}
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import mouse.all._
import org.apache.commons.csv.CSVFormat
import velocorner.api.brand.{Brand, BrandUrl}

import java.io.FileReader
import scala.jdk.CollectionConverters._
import scala.util.control.Exception._
import scala.util.{Failure, Success, Try}

/**
 * Convert the scraping format into the model.
 */
//noinspection TypeAnnotation
object BrandImport extends LazyLogging {

  val home = sys.props.get("user.home").getOrElse("/Users/levi")
  val dir = s"$home/Downloads/velo/velocorner/brands"

  private val imageTypes = Set("jpg", "jpeg", "png", "gif")

  type CsvRow = Array[String]
  case class Csv(header: CsvRow, rows: List[CsvRow])

  def load(file: String)(convert: CsvRow => List[BrandUrl]): List[BrandUrl] = {
    val in = new FileReader(dir + "/" + file)
    ultimately {
      in.close()
    }.apply {
      val records = CSVFormat.EXCEL.parse(in).getRecords.asScala.map(_.toList.asScala.toArray).toList
      val csv = Csv(records.head, records.tail)
      val entries: Validated[NonEmptyList[String], List[BrandUrl]] = csv.rows
        .map { row =>
          Try(convert(row)) match {
            case Success(brandUrls) => brandUrls.valid[String]
            case Failure(e)         => s"${e.getMessage} - ${row.mkString(",")}".invalid[List[BrandUrl]]
          }
        }
        .traverse(_.toValidatedNel)
        .map(_.flatten)
      entries match {
        case Validated.Valid(entries)  => entries
        case Validated.Invalid(errors) => throw new RuntimeException(errors.toList.mkString("\t"))
      }
    }
  }

  // name, url
  def wiggle(file: String): List[BrandUrl] = load(file) { row =>
    List(
      BrandUrl(
        brand = Brand(row(0), none),
        url = row(1)
      )
    )
  }

  // first columns: name1, url1, ignore, url2, image2, name2, image1, url3...
  // url, [image], name -> repeats
  def performanceBike(file: String): List[BrandUrl] = load(file) { row =>
    val first2 = List(
      BrandUrl(
        brand = Brand(row(0), Option.when(!row(6).endsWith("content/t.gif"))(row(6))),
        url = row(1)
      ),
      BrandUrl(
        brand = Brand(row(5), Option.when(!row(4).endsWith("content/t.gif"))(row(4))),
        url = row(3)
      )
    )
    val rest = row.drop(7)
    def extract(list: Array[String]): List[BrandUrl] = list match {
      case l3 if l3.length >= 3 =>
        // println(l3.take(3).mkString("\t"))
        val first = l3(0).trim
        val second = l3(1).trim
        val third = l3(2).trim
        val isSecondImage = imageTypes.exists(second.toLowerCase.endsWith)
        val items =
          if (first.isEmpty) List.empty
          else {
            val isValidImage = isSecondImage && !second.endsWith("content/t.gif") // an empty image
            val item = BrandUrl(
              brand = Brand(isSecondImage.fold(third, second), Option.when(isValidImage)(second)),
              url = first
            )
            if (item.brand.name.isEmpty || item.brand.name.contains("http")) {
              logger.warn(s"invalid brand: $item on ${l3.mkString("\t")}")
              List.empty
            } else List(item)
          }
        items ::: extract(l3.drop(isSecondImage.fold(3, 2)))
      case _ => List.empty
    }
    first2 ++ extract(rest)
  }

  def bikeComponents(file: String): List[BrandUrl] = headingUrlName(file)
  def chainReaction(file: String): List[BrandUrl] = headingUrlName(file)

  // heading, url, name, url2, name2, ...
  private def headingUrlName(file: String): List[BrandUrl] = load(file) { row =>
    def extract(list: Array[String]): List[BrandUrl] = list.map(_.trim).filter(_.nonEmpty) match {
      case Array(l1, l2, _*) => List(BrandUrl(brand = Brand(l2, none), url = l1)) ++ extract(list.drop(2))
      case _                 => List.empty
    }
    extract(row.drop(1))
  }

  // name1, url1, heading, url2, name2, ...
  def bikester(file: String): List[BrandUrl] = load(file) { row =>
    def extract(list: Array[String]): List[BrandUrl] = list.map(_.trim).filter(_.nonEmpty) match {
      case Array(l1, l2, _*) => List(BrandUrl(brand = Brand(l2, none), url = l1)) ++ extract(list.drop(2))
      case _                 => List.empty
    }
    BrandUrl(Brand(row(0), none), row(1)) +: extract(row.drop(3))
  }

  // name1, url1, ...
  def bike24(file: String): List[BrandUrl] = load(file) { row =>
    def extract(list: Array[String]): List[BrandUrl] = list.map(_.replace("\n", "").trim).filter(_.nonEmpty) match {
      case Array(l1, l2, _*) => List(BrandUrl(brand = Brand(l1, none), url = l2)) ++ extract(list.drop(2))
      case _                 => List.empty
    }
    extract(row)
  }
}
