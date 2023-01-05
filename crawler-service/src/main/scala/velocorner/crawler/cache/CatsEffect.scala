package velocorner.crawler.cache

import cats.effect.Async
import scalacache.Mode

import scala.util.control.NonFatal

// "com.github.cb372" %% "scalacache-cats-effect" conflicts with the current version
object CatsEffect {

  object modes {

    /**
     * A mode that wraps computations in F[_],
     * where there is an instance of cats-effect Async available for F.
     * This includes the cats-effect `IO[_]` type.
     */
    implicit def async[F[_]](implicit F: Async[F]): Mode[F] = new Mode[F] {
      val M: scalacache.Async[F] = asyncForCatsEffectAsync[F]
    }

  }

  def asyncForCatsEffectAsync[F[_]](implicit af: Async[F]): scalacache.Async[F] = new scalacache.Async[F] {

    def pure[A](a: A): F[A] = af.pure(a)

    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = af.flatMap(fa)(f)

    def map[A, B](fa: F[A])(f: A => B): F[B] = af.map(fa)(f)

    def raiseError[A](t: Throwable): F[A] = af.raiseError(t)

    def handleNonFatal[A](fa: => F[A])(f: Throwable => A): F[A] = af.recover(fa) { case NonFatal(e) =>
      f(e)
    }

    def delay[A](thunk: => A): F[A] = af.delay(thunk)

    def suspend[A](thunk: => F[A]): F[A] = af.defer(thunk)

    def async[A](register: (Either[Throwable, A] => Unit) => Unit): F[A] = af.async_(register)
  }
}
