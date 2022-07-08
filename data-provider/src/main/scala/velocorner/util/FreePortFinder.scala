package velocorner.util

import java.net.ServerSocket

object FreePortFinder extends CloseableResource {

  def find(): Int =
    withCloseable(new ServerSocket(0)) { ss =>
      ss.setReuseAddress(true)
      ss.getLocalPort
    }
}
