package com.github.al.roulette.load

import com.github.al.roulette.bet.api.BetService
import com.github.al.roulette.game.api.GameService
import com.github.al.roulette.load.api.LoadTestService
import com.github.al.roulette.load.impl.LoadTestServiceImpl
import com.github.al.roulette.player.api.PlayerService
import com.github.al.roulette.winnings.api.WinningsService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaClientComponents
import com.lightbend.lagom.scaladsl.client.ServiceClient
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.pubsub.PubSubComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire.wire
import com.typesafe.conductr.bundlelib.lagom.scaladsl.ConductRApplicationComponents
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext


trait LoadTestComponents
  extends LagomServerComponents
    with PubSubComponents {
  implicit def serviceClient: ServiceClient
  implicit override def executionContext: ExecutionContext

  private lazy val gameService: GameService = serviceClient.implement[GameService]
  private lazy val betService: BetService = serviceClient.implement[BetService]
  private lazy val playerService: PlayerService = serviceClient.implement[PlayerService]
  private lazy val winningsService: WinningsService = serviceClient.implement[WinningsService]
  private lazy val akkaActorScheduler = actorSystem.scheduler

  override lazy val lagomServer: LagomServer = serverFor[LoadTestService](wire[LoadTestServiceImpl])
}

abstract class LoadTestApplication(context: LagomApplicationContext) extends LagomApplication(context)
  with LoadTestComponents
  with AhcWSComponents
  with LagomKafkaClientComponents

class LoadTestApplicationLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext) =
    new LoadTestApplication(context) with ConductRApplicationComponents

  override def loadDevMode(context: LagomApplicationContext) =
    new LoadTestApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[LoadTestService])
}
