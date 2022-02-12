package velocorner.feed

import cats.data.{NonEmptyList, Validated}
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import mouse.all._
import org.apache.commons.csv.CSVFormat
import velocorner.model.brand.{Brand, BrandUrl}

import java.io.FileReader
import scala.jdk.CollectionConverters._
import scala.util.control.Exception._
import scala.util.{Failure, Success, Try}

//noinspection TypeAnnotation
object BrandFeed extends LazyLogging {

  val home = sys.props.get("user.home").getOrElse("/Users/levi")
  val dir = s"$home/Downloads/velo/velocorner/brands"

  val imageTypes = Set("jpg", "jpeg", "png", "gif")

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
        //println(l3.take(3).mkString("\t"))
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
}
