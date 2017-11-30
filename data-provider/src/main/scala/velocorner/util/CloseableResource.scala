package velocorner.util

import java.io.Closeable

// try with resources pattern for scala
trait CloseableResource {

  def withCloseable[R, C <: Closeable](c: C)(body: C => R): R = try {
    body(c)
  } finally {
    c.close()
  }
}
