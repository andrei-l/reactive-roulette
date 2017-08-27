import Dependencies._
import sbt.Keys._

lagomCassandraCleanOnStart in ThisBuild := false
lagomKafkaEnabled in ThisBuild := true

scalaVersion in ThisBuild := "2.11.8"


lazy val `reactive-roulette` = (project in file("."))
  .aggregate(`roulette-game-api`, `roulette-game-impl`)
  .aggregate(`game-scheduler-api`, `game-scheduler-impl`)
  .aggregate(`roulette-bets-api`, `roulette-bets-impl`)
  .aggregate(`player-winnings-api`, `player-winnings-impl`)
  .aggregate(`players-api`, `players-impl`)
  .aggregate(`json-extensions`, `persistence-extensions`)
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
      macwire,
      scalaTest
    )
  )
  .enablePlugins(LagomScala)
  .dependsOn(`persistence-extensions`)
  .dependsOn(`roulette-game-api`)
  .dependsOn(`game-scheduler-api`)

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
      macwire,
      scalaTest
    )
  )
  .enablePlugins(LagomScala)
  .dependsOn(`game-scheduler-api`)
  .dependsOn(`roulette-game-api`)

lazy val `roulette-bets-api` = (project in file("roulette-bets-api"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs
    )
  ).dependsOn(`json-extensions`)

lazy val `roulette-bets-impl` = (project in file("roulette-bets-impl"))
  .settings(commonSettings)
  .settings(lagomForkedTestSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      lagomScaladslKafkaBroker,
      datastaxCassandraDriverExtras,
      macwire,
      scalaTest
    )
  )
  .enablePlugins(LagomScala)
  .dependsOn(`persistence-extensions`)
  .dependsOn(`roulette-game-api`)
  .dependsOn(`roulette-bets-api`)
  .dependsOn(`player-winnings-api`)

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
      macwire,
      scalaTest
    )
  )
  .enablePlugins(LagomScala)
  .dependsOn(`persistence-extensions`)
  .dependsOn(`player-winnings-api`)
  .dependsOn(`roulette-game-api`)
  .dependsOn(`players-api`)

lazy val `players-api` = (project in file("players-api"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs
    )
  ).dependsOn(`json-extensions`)

lazy val `players-impl` = (project in file("players-impl"))
  .settings(commonSettings)
  .settings(lagomForkedTestSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      lagomScaladslKafkaBroker,
      datastaxCassandraDriverExtras,
      macwire,
      scalaTest
    )
  )
  .enablePlugins(LagomScala)
  .dependsOn(`persistence-extensions`)
  .dependsOn(`players-api`)

lazy val `json-extensions` = (project in file("json-extensions"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs,
      scalaTest
    )
  )

lazy val `persistence-extensions` = (project in file("persistence-extensions"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra % Provided
    )
  )

lazy val commonSettings = List(
  organization := "com.github.al",
  version := "1.0-SNAPSHOT",
  scalastyleFailOnError := true,
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports"),
  scalacOptions ++= Seq("-feature", "-deprecation"),
  libraryDependencies += mockitoCore
)
