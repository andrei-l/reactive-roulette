package com.github.al.roulette.scheduler

import com.github.al.roulette.game.api.GameService
import com.github.al.roulette.scheduler.api.GameSchedulerService
import com.github.al.roulette.scheduler.impl.{GameScheduler, GameSchedulerServiceImpl}
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.client.ServiceClient
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.EmptyJsonSerializerRegistry
import com.lightbend.lagom.scaladsl.pubsub.PubSubComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire.wire
import com.typesafe.conductr.bundlelib.lagom.scaladsl.ConductRApplicationComponents
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext


trait SchedulerComponents extends LagomServerComponents
  with PubSubComponents
  with CassandraPersistenceComponents
  with AhcWSComponents {

  override implicit def executionContext: ExecutionContext
  implicit def serviceClient: ServiceClient

  override lazy val lagomServer: LagomServer = serverFor[GameSchedulerService](wire[GameSchedulerServiceImpl])
  override lazy val jsonSerializerRegistry = EmptyJsonSerializerRegistry

  lazy val gameService: GameService = serviceClient.implement[GameService]
  lazy val gameScheduler: GameScheduler = wire[GameScheduler]
}

abstract class SchedulerApplication(context: LagomApplicationContext) extends LagomApplication(context)
  with SchedulerComponents
  with LagomKafkaComponents {
  gameScheduler
}

class SchedulerApplicationLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext) =
    new SchedulerApplication(context) with ConductRApplicationComponents

  override def loadDevMode(context: LagomApplicationContext) =
    new SchedulerApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[GameService])
}
