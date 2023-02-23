package console

import scala.Console.{ BLACK_B, GREEN_B, YELLOW_B, RED_B, WHITE, RESET }

object Console {

  def info(s: String) {
    println(s"${RESET}${GREEN_B}${WHITE}[INFO]: ${s} ${RESET}")
  }

  def warn(s: String) {
    println(s"${RESET}${YELLOW_B}${WHITE}[WARN]: ${s} ${RESET}")
  }

  def error(s: String) {
    println(s"${RESET}${RED_B}${WHITE}[ERROR]: ${s} ${RESET}")
  }

}
