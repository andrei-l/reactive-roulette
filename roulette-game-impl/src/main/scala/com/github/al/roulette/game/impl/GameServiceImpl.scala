package com.github.al.roulette.game.impl

import java.util.UUID

import akka.{Done, NotUsed}
import com.github.al.roulette.game.api.{Game, GameService}
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.Future

class GameServiceImpl extends GameService {
  override def createGame: ServiceCall[Game, UUID] = ServiceCall {
    _ => Future.successful(UUID.fromString("7e595fac-830e-44f1-b73e-f8fd60594ace"))
  }

  override def terminateGame(id: UUID): ServiceCall[NotUsed, Done] =  ServiceCall {
    _ => Future.successful(Done)
  }

}
