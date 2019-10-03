package velocorner.util

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.{Path, Paths}
import java.util.zip.{ZipEntry, ZipOutputStream}

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.IOUtils

object FileUtils extends CloseableResource with LazyLogging {

  def createTempDir(tempName: String): String = {
    val tmpDir = Paths.get(System.getProperty("java.io.tmpdir"))
    val name: Path = tmpDir.getFileSystem.getPath(tempName)
    if (name.getParent != null) throw new IllegalArgumentException("Invalid name")
    tmpDir.resolve(name).toString
  }

  def zipDir(dirName: String, fileName: String): Unit = {
    withCloseable(new ZipOutputStream(new FileOutputStream(fileName))) { zos =>
      addDirToArchive(zos, new File(dirName))
    }
  }

  private def addDirToArchive(zos: ZipOutputStream, file: File): Unit = {
    file.listFiles().foreach{entry =>
      if (entry.isDirectory) {
        logger.debug(s"adding directory ${entry.getName} to archive")
        addDirToArchive(zos, entry)
      } else {
        logger.debug(s"adding file ${entry.getName} to archive")
        withCloseable(new FileInputStream(entry)) { fis =>
          zos.putNextEntry(new ZipEntry(entry.getName))
          IOUtils.copy(fis, zos)
          zos.closeEntry()
        }
      }
    }
  }
}
