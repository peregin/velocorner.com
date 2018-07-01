package velocorner.util

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.{Path, Paths}
import java.util.zip.{ZipEntry, ZipOutputStream}

import org.apache.commons.io.IOUtils
import org.slf4s.Logging

object FileUtils extends CloseableResource with Logging {

  def createTempDir(tempName: String): String = {
    val tmpDir = Paths.get(System.getProperty("java.io.tmpdir"))
    val name: Path = tmpDir.getFileSystem.getPath(tempName)
    if (name.getParent != null) throw new IllegalArgumentException("Invalid name")
    tmpDir.resolve(name).toString
  }

  def zipDir(dirName: String, fileName: String) {
    withCloseable(new ZipOutputStream(new FileOutputStream(fileName))) { zos =>
      addDirToArchive(zos, new File(dirName))
    }
  }

  private def addDirToArchive(zos: ZipOutputStream, file: File) {
    file.listFiles().foreach{entry =>
      if (entry.isDirectory) {
        log.debug(s"adding directory ${entry.getName()} to archive")
        addDirToArchive(zos, entry)
      } else {
        log.debug(s"adding file ${entry.getName()} to archive")
        withCloseable(new FileInputStream(entry)) { fis =>
          zos.putNextEntry(new ZipEntry(entry.getName()));
          IOUtils.copy(fis, zos)
          zos.closeEntry()
        }
      }
    }
  }
}
