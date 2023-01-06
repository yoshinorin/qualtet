import wartremover.{Wart, Warts}

// https://www.wartremover.org/
object Wartremover {

  val rules = Warts.allBut(
      Wart.StringPlusAny,
      Wart.Throw,
      Wart.DefaultArguments,
      Wart.Overloading,
      Wart.Nothing
    )

}
