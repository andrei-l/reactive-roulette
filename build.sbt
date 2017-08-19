import Dependencies._
import sbt.Keys._

lagomCassandraPort in ThisBuild := 9042
lagomCassandraCleanOnStart in ThisBuild := true
lagomKafkaEnabled in ThisBuild := false

scalaVersion in ThisBuild := "2.11.8"


lazy val `reactive-roulette` = (project in file("."))
  .aggregate(`roulette-game-api`, `roulette-game-impl`)
  .aggregate(`json-extensions`)
  .settings(commonSettings)
  .settings(name := "reactive-roulette")


lazy val `roulette-game-api` = (project in file("roulette-game-api"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  ).dependsOn(`json-extensions`)

lazy val `roulette-game-impl` = (project in file("roulette-game-impl"))
  .settings(commonSettings)
  .settings(lagomForkedTestSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      datastaxCassandraDriverExtras,
      macwire,
      scalaTest
    )
  )
  .enablePlugins(LagomScala)
  .dependsOn(`roulette-game-api`)

lazy val `json-extensions` = (project in file("json-extensions"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      scalaTest
    )
  )

lazy val commonSettings = List(
  organization := "com.github.al",
  version := "1.0-SNAPSHOT",
  scalastyleFailOnError := true,
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports"),
  scalacOptions ++= Seq("-feature", "-deprecation")
)
