import sbt._
import scala.sys.process._
import java.lang.ProcessHandle
import java.util.NoSuchElementException
import scala.Console.{ BLACK_B, GREEN_B, RED_B, WHITE, RESET }

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
      case None => println(s"${RESET}${RED_B}${WHITE}\n ---- Local server is not running ${RESET}")
      case Some(pid) => {
        println(s"${RESET}${GREEN_B}${WHITE}\n ---- killing PID: ${pid} ${RESET}")
        try {
          ProcessHandle
            .allProcesses
            .filter(p => p.pid() == pid)
            .findFirst()
            .get()
            .destroy()
          println(s"${RESET}${GREEN_B}${WHITE}\n ---- killed PID: ${pid} ${RESET}")
        } catch {
          case ne: NoSuchElementException => {
            println(s"${RESET}${RED_B}${WHITE}\n ---- Local server is not running ${RESET}")
          }
          case _: Throwable => {
            println(s"${RESET}${RED_B}${WHITE}\n ---- Something went to wrong... ${RESET}")
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
