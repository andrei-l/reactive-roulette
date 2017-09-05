import sbt._


object Dependencies {
  lazy val macwire: ModuleID = "com.softwaremill.macwire" %% "macros" % "2.2.5" % Provided
  lazy val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.1" % Test
  lazy val scalaCheck: ModuleID = "org.scalacheck" %% "scalacheck" % "1.13.5" % Test
  lazy val mockitoCore: ModuleID = "org.mockito" % "mockito-core" % "2.9.0" % Test
  lazy val datastaxCassandraDriverExtras: ModuleID = "com.datastax.cassandra" % "cassandra-driver-extras" % "3.0.0"
  lazy val playJsonDerivedCodecs: ModuleID = "org.julienrf" %% "play-json-derived-codecs" % "3.3"
  lazy val jwtPlayJson: ModuleID = "com.pauldijou" %% "jwt-play-json" % "0.14.0"
}
