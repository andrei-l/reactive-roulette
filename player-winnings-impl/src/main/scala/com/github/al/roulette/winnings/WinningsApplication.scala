package com.github.al.roulette.winnings

import com.github.al.roulette.bet.api.BetService
import com.github.al.roulette.game.api.GameService
import com.github.al.roulette.winnings.api.WinningsService
import com.github.al.roulette.winnings.impl.{GameBetsProclamationEventsSubscriber, GameResultEventsSubscriber, WinningsEntity, WinningsServiceImpl}
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.client.ServiceClient
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire.wire
import com.typesafe.conductr.bundlelib.lagom.scaladsl.ConductRApplicationComponents
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext


trait WinningsComponents extends LagomServerComponents
  with CassandraPersistenceComponents {

  implicit def executionContext: ExecutionContext

  implicit def serviceClient: ServiceClient

  override lazy val lagomServer: LagomServer = serverFor[WinningsService](wire[WinningsServiceImpl])
  override lazy val jsonSerializerRegistry = WinningsSerializerRegistry

  lazy val gameService: GameService = serviceClient.implement[GameService]
  lazy val betService: BetService = serviceClient.implement[BetService]

  lazy val gameResultEventsSubscriber: GameResultEventsSubscriber = wire[GameResultEventsSubscriber]
  lazy val gameBetsProclamationEventsSubscriber: GameBetsProclamationEventsSubscriber = wire[GameBetsProclamationEventsSubscriber]

  persistentEntityRegistry.register(wire[WinningsEntity])
}

abstract class WinningsApplication(context: LagomApplicationContext) extends LagomApplication(context)
  with WinningsComponents
  with AhcWSComponents
  with LagomKafkaComponents {
  gameResultEventsSubscriber
  gameBetsProclamationEventsSubscriber
}

class WinningsApplicationLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext) =
    new WinningsApplication(context) with ConductRApplicationComponents

  override def loadDevMode(context: LagomApplicationContext) =
    new WinningsApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[WinningsService])
}
