package com.github.al.roulette.bet

import com.github.al.roulette.bet.api.BetService
import com.github.al.roulette.bet.impl.{BetServiceImpl, GameEventsSubscriber, RouletteGameBetsEntity}
import com.github.al.roulette.game.api.GameService
import com.github.al.roulette.player.api.PlayerService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.client.ServiceClient
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire.wire
import com.typesafe.conductr.bundlelib.lagom.scaladsl.ConductRApplicationComponents
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext


trait BetComponents extends LagomServerComponents
  with CassandraPersistenceComponents {

  implicit def executionContext: ExecutionContext

  implicit def serviceClient: ServiceClient

  override lazy val lagomServer: LagomServer = serverFor[BetService](wire[BetServiceImpl])
  override lazy val jsonSerializerRegistry = BetSerializerRegistry

  lazy val gameService: GameService = serviceClient.implement[GameService]
  lazy val gameEventsSubscriber: GameEventsSubscriber = wire[GameEventsSubscriber]

  persistentEntityRegistry.register(wire[RouletteGameBetsEntity])
}

abstract class BetApplication(context: LagomApplicationContext) extends LagomApplication(context)
  with BetComponents
  with AhcWSComponents
  with LagomKafkaComponents {
  gameEventsSubscriber
}

class BetApplicationLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext) =
    new BetApplication(context) with ConductRApplicationComponents

  override def loadDevMode(context: LagomApplicationContext) =
    new BetApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[PlayerService])
}
