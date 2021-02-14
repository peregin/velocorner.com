package velocorner.analytics.setup

import cats.Semigroup
import cats.data._
import cats.implicits._
import shapeless._
import velocorner.analytics.setup.Converter.SValidated

import scala.collection.immutable.{:: => Cons}

case class Description(name: String, types: HList)

case class Input(id: String, distance: Double, name: Option[String])

trait Converter[T] {

  def name: String

  def decode(v: List[String]): SValidated[T]
}

abstract class NamedConverter[T](val name: String) extends Converter[T] {
  override def toString: String = name
}

object Converter {

  type SValidated[T] = Validated[String, T]

  def apply[T](name: String, fun: String => SValidated[T]): Converter[T] = new NamedConverter[T](name) {
    override def decode(v: List[String]): SValidated[T] = v.headOption.map(fun).getOrElse("empty value".invalid)
  }
}

object Setup extends App {

  implicit def deriveString: Converter[String] = Converter.apply("string", _.valid[String])

  implicit def deriveDouble: Converter[Double] = Converter.apply("double", _.toDouble.valid[String])

  implicit def deriveOptionalString: Converter[Option[String]] = Converter.apply("opstring", Option(_).filter(_.nonEmpty).valid[String])

  implicit def deriveHNil: Converter[HNil] = new NamedConverter[HNil]("") {
    def decode(str: List[String]): SValidated[HNil] = str match {
      case Cons("", _) => HNil.valid[String]
      case s           => s"Cannot convert '$s' to HNil".invalid[HNil]
    }
  }

  implicit def deriveHCons[V, T <: HList](implicit
      scv: Lazy[Converter[V]],
      sct: Lazy[Converter[T]]
  ): Converter[V :: T] =
    new NamedConverter[V :: T](s"${scv.value.name} ${sct.value.name}") {
      def decode(str: List[String]): SValidated[V :: T] = str match {
        case Cons(before, after) =>
          val front = scv.value.decode(List(before))
          val back = sct.value.decode(after)
          (front, back) match {
            case (Validated.Valid(a), Validated.Valid(b))       => (a :: b).valid[String]
            case (Validated.Valid(a), i @ Validated.Invalid(b)) => b.invalid[V :: T]
            case (Validated.Invalid(a), Validated.Valid(b))     => a.invalid[V :: T]
            case (Validated.Invalid(a), Validated.Invalid(b))   => Semigroup.combine(a, b).invalid[V :: T]
          }
        case s => s"Cannot convert '$s' to HNil".invalid[V :: T]
      }
    }

  def parse[T](in: List[String])(implicit converter: Converter[T]): SValidated[T] = converter.decode(in)

  def read(in: List[String]): Input = {
    val test = Input("1", 12.3, "Bike".some)
    val gen = Generic[Input].to(test) //.from(in)
    Generic[Input].from(gen)
  }

  val res = parse[String :: Double :: Option[String] :: HNil](List("1", "12.3", "Biking"))
  println(s"res=$res")
}
