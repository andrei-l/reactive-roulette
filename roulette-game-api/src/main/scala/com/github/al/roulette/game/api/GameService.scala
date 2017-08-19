package com.github.al.roulette.game.api

import java.util.UUID

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait GameService extends Service {
  def createGame: ServiceCall[Game, UUID]

  def terminateGame(id: UUID): ServiceCall[NotUsed, Done]

  final override def descriptor: Descriptor = {
    import Service._

    named("game").withCalls(
      pathCall("/api/recurring-game", createGame),
      restCall(Method.POST, "/api/recurring-game/:id/terminate", terminateGame _)
    ).withAutoAcl(true)
  }
}
