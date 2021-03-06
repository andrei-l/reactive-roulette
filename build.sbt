import Dependencies.{scalaTest, _}
import sbt.Keys._

lagomCassandraCleanOnStart in ThisBuild := true
lagomKafkaEnabled in ThisBuild := true

scalaVersion in ThisBuild := "2.11.8"


lazy val `reactive-roulette` = (project in file("."))
  .aggregate(`roulette-game-api`, `roulette-game-impl`)
  .aggregate(`game-scheduler-api`, `game-scheduler-impl`)
  .aggregate(`roulette-bet-api`, `roulette-bet-impl`)
  .aggregate(`player-winnings-api`, `player-winnings-impl`)
  .aggregate(`player-api`, `player-impl`)
  .aggregate(`load-test-api`, `load-test-impl`)
  .aggregate(extensions)
  .settings(commonSettings)
  .settings(name := "reactive-roulette")

lazy val `roulette-game-api` = (project in file("roulette-game-api"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs
    )
  ).dependsOn(`json-extensions`)

lazy val `roulette-game-impl` = (project in file("roulette-game-impl"))
  .settings(commonSettings)
  .settings(lagomForkedTestSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      lagomScaladslKafkaBroker,
      datastaxCassandraDriverExtras,
      macwire
    )
  )
  .enablePlugins(LagomScala)
  .dependsOn(`persistence-extensions`)
  .dependsOn(`service-logging-extensions`)
  .dependsOn(`roulette-game-api`)
  .dependsOn(`game-scheduler-api`)
  .dependsOn(`tests-extensions` % "test->test")

lazy val `game-scheduler-api` = (project in file("game-scheduler-api"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs
    )
  ).dependsOn(`json-extensions`)

lazy val `game-scheduler-impl` = (project in file("game-scheduler-impl"))
  .settings(commonSettings)
  .settings(lagomForkedTestSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      lagomScaladslKafkaBroker,
      lagomScaladslPubSub,
      macwire
    )
  )
  .enablePlugins(LagomScala)
  .dependsOn(`service-logging-extensions`)
  .dependsOn(`game-scheduler-api`)
  .dependsOn(`roulette-game-api`)
  .dependsOn(`tests-extensions` % "test->test")

lazy val `roulette-bet-api` = (project in file("roulette-bet-api"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs
    )
  ).dependsOn(`json-extensions`)

lazy val `roulette-bet-impl` = (project in file("roulette-bet-impl"))
  .settings(commonSettings)
  .settings(lagomForkedTestSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      lagomScaladslKafkaBroker,
      datastaxCassandraDriverExtras,
      macwire
    )
  )
  .enablePlugins(LagomScala)
  .dependsOn(`persistence-extensions`)
  .dependsOn(`authentication-extensions`)
  .dependsOn(`service-logging-extensions`)
  .dependsOn(`roulette-game-api`)
  .dependsOn(`roulette-bet-api`)
  .dependsOn(`tests-extensions` % "test->test")

lazy val `player-winnings-api` = (project in file("player-winnings-api"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs
    )
  ).dependsOn(`json-extensions`)

lazy val `player-winnings-impl` = (project in file("player-winnings-impl"))
  .settings(commonSettings)
  .settings(lagomForkedTestSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      lagomScaladslKafkaBroker,
      datastaxCassandraDriverExtras,
      macwire
    )
  )
  .enablePlugins(LagomScala)
  .dependsOn(`persistence-extensions`)
  .dependsOn(`service-logging-extensions`)
  .dependsOn(`player-winnings-api`)
  .dependsOn(`roulette-bet-api`)
  .dependsOn(`roulette-game-api`)
  .dependsOn(`tests-extensions` % "test->test")

lazy val `player-api` = (project in file("player-api"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs
    )
  ).dependsOn(`json-extensions`)

lazy val `player-impl` = (project in file("player-impl"))
  .settings(commonSettings)
  .settings(lagomForkedTestSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      lagomScaladslKafkaBroker,
      datastaxCassandraDriverExtras,
      macwire
    )
  )
  .enablePlugins(LagomScala)
  .dependsOn(`persistence-extensions`)
  .dependsOn(`service-logging-extensions`)
  .dependsOn(`authentication-extensions`)
  .dependsOn(`player-api`)
  .dependsOn(`tests-extensions` % "test->test")


lazy val `load-test-api` = (project in file("load-test-api"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs
    )
  ).dependsOn(`json-extensions`)

lazy val `load-test-impl` = (project in file("load-test-impl"))
  .settings(commonSettings)
  .settings(lagomForkedTestSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      lagomScaladslKafkaClient,
      lagomScaladslPubSub,
      macwire
    )
  )
  .enablePlugins(LagomScala)
  .dependsOn(`load-test-api`)
  .dependsOn(`roulette-game-api`)
  .dependsOn(`roulette-bet-api`)
  .dependsOn(`player-api`)
  .dependsOn(`player-winnings-api`)
  .dependsOn(`service-logging-extensions`)

lazy val extensions = (project in file("extensions"))
  .aggregate(`json-extensions`)
  .aggregate(`persistence-extensions`)
  .aggregate(`service-logging-extensions`)
  .aggregate(`authentication-extensions`)
  .aggregate(`tests-extensions`)
  .settings(commonSettings)

lazy val `json-extensions` = (project in file("extensions/json-extensions"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs
    )
  )

lazy val `persistence-extensions` = (project in file("extensions/persistence-extensions"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra % Provided
    )
  )

lazy val `authentication-extensions` = (project in file("extensions/authentication-extensions"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      jwtPlayJson
    )
  ).dependsOn(`service-logging-extensions`)

lazy val `service-logging-extensions` = (project in file("extensions/service-logging-extensions"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslServer,
      lagomScaladslPersistenceCassandra % Provided,
      scalaLogging
    )
  )

lazy val `tests-extensions` = (project in file("extensions/tests-extensions"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      lagomScaladslPersistenceCassandra % Provided
    )
  )

lazy val commonSettings = List(
  organization := "com.github.al",
  version := "1.0-SNAPSHOT",
  scalastyleFailOnError := true,
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports"),
  scalacOptions ++= Seq("-feature", "-deprecation"),
  libraryDependencies ++= Seq(scalaTest, scalaCheck, mockitoCore)

)
