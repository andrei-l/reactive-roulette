import Dependencies._
import sbt.Keys._

lagomCassandraCleanOnStart in ThisBuild := false
lagomKafkaEnabled in ThisBuild := true

scalaVersion in ThisBuild := "2.11.8"


lazy val `reactive-roulette` = (project in file("."))
  .aggregate(`roulette-game-api`, `roulette-game-impl`)
  .aggregate(`game-scheduler-api`, `game-scheduler-impl`)
  .aggregate(`json-extensions`)
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

lazy val `json-extensions` = (project in file("json-extensions"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs,
      scalaTest
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
