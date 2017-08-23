package com.github.al.roulette.game

import com.github.al.roulette.game.api.GameService
import com.github.al.roulette.game.impl.{GameEntity, GameSerializerRegistry, GameServiceImpl}
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire.wire
import com.typesafe.conductr.bundlelib.lagom.scaladsl.ConductRApplicationComponents
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext


trait GameComponents extends LagomServerComponents
  with CassandraPersistenceComponents {

  implicit def executionContext: ExecutionContext

  override lazy val lagomServer: LagomServer = serverFor[GameService](wire[GameServiceImpl])
  override lazy val jsonSerializerRegistry = GameSerializerRegistry

  persistentEntityRegistry.register(wire[GameEntity])
}

abstract class GameApplication(context: LagomApplicationContext) extends LagomApplication(context)
  with GameComponents
  with AhcWSComponents
  with LagomKafkaComponents {
}

class GameApplicationLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext) =
    new GameApplication(context) with ConductRApplicationComponents

  override def loadDevMode(context: LagomApplicationContext) =
    new GameApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[GameService])
}
