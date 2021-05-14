logLevel := util.Level.Warn

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")
addSbtPlugin("au.com.onegeek" % "sbt-dotenv" % "2.1.204")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
// TODO: https://github.com/pureconfig/pureconfig/pull/799
addSbtPlugin("net.ruippeixotog" % "sbt-coveralls" % "1.3.0")
