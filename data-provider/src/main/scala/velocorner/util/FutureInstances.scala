package velocorner.util

import scalaz.Monad

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object FutureInstances {

  implicit val monadInstance = new Monad[Future] {
    override def point[A](a: => A): Future[A] = Future(a)
    override def bind[A, B](fa: Future[A])(f: A => Future[B]): Future[B] = fa.flatMap(f)
  }
}
