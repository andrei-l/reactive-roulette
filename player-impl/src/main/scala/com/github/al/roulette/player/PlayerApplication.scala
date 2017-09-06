package com.github.al.roulette.player

import com.github.al.roulette.player.api.PlayerService
import com.github.al.roulette.player.impl.{PlayerEntity, PlayerEventReadSideProcessor, PlayerRepository, PlayerServiceImpl}
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.client.ServiceClient
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire.wire
import com.typesafe.conductr.bundlelib.lagom.scaladsl.ConductRApplicationComponents
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext


trait PlayerComponents extends LagomServerComponents
  with CassandraPersistenceComponents {

  implicit def executionContext: ExecutionContext

  implicit def serviceClient: ServiceClient

  override lazy val lagomServer: LagomServer = serverFor[PlayerService](wire[PlayerServiceImpl])
  override lazy val jsonSerializerRegistry = PlayerSerializerRegistry
  private lazy val playerRepository = wire[PlayerRepository]

  persistentEntityRegistry.register(wire[PlayerEntity])
  readSide.register(wire[PlayerEventReadSideProcessor])
}

abstract class PlayerApplication(context: LagomApplicationContext) extends LagomApplication(context)
  with PlayerComponents
  with AhcWSComponents
  with LagomKafkaComponents

class PlayerApplicationLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext) =
    new PlayerApplication(context) with ConductRApplicationComponents

  override def loadDevMode(context: LagomApplicationContext) =
    new PlayerApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[PlayerService])
}
