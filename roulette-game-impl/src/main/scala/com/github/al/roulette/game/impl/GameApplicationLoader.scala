package com.github.al.roulette.game.impl

import com.github.al.roulette.game.api.GameService
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire.wire
import play.api.libs.ws.ahc.AhcWSComponents

import com.typesafe.conductr.bundlelib.lagom.scaladsl.ConductRApplicationComponents


abstract class GameApplication(context: LagomApplicationContext)  extends LagomApplication(context)
    with AhcWSComponents
    with CassandraPersistenceComponents {

  override lazy val lagomServer: LagomServer = serverFor[GameService](wire[GameServiceImpl])
  override lazy val jsonSerializerRegistry = GameSerializerRegistry


}

class GameApplicationLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext) =
    new GameApplication(context) with ConductRApplicationComponents

  override def loadDevMode(context: LagomApplicationContext) =
    new GameApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[GameService])
}
