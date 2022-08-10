logLevel := util.Level.Warn

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("nl.gn0s1s" % "sbt-dotenv" % "3.0.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.3")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.2")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.2.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-license-report" % "1.2.0")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "3.0.5")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scala3-migrate" % "0.5.1")
