import sbt._
import scala.sys.process._
import java.lang.ProcessHandle
import java.util.NoSuchElementException

object LocalProcesses {

  val lineSeparator = System.lineSeparator

  private def javaProcesses(): Array[String] = "jps -l".!!.split(lineSeparator)

  private def getDevServerPID(ps: Array[String]): Option[Long] = {
    val psStrings = ps.filter(p => p.contains("net.yoshinorin.qualtet.BootStrap"))
    if (psStrings.length > 0) {
      Some(psStrings.head.split(" ").head.toLong)
    } else {
      None
    }
  }

  def killLocalServer(maybePid: Option[Long]): Unit = {
    maybePid match {
      case None => println(s" ---- Local server is not running")
      case Some(pid) => {
        println(s"\n ---- killing PID: ${pid}")
        try {
          ProcessHandle
            .allProcesses
            .filter(p => p.pid() == pid)
            .findFirst()
            .get()
            .destroy()
          println(s"\n ---- killed PID: ${pid}")
        } catch {
          case ne: NoSuchElementException => {
            println(s" ---- Local server is not running")
          }
          case _: Throwable => {
            println(" ---- Something went to wrong...")
          }
        }
      }
    }
  }

  lazy val kill = taskKey[Unit]("kill current local server process")

  val tasks = Seq(
    kill := killLocalServer(getDevServerPID(javaProcesses()))
  )

  object Commands {
    val startLocalServer = {
      """
        |;scalafmt
        |;Test / scalafmt
        |;forceKillServer
        |;~reStart
        |""".stripMargin
    }

    val kill = {
      """
        |;forceKillServer
      """.stripMargin
    }
  }
}
