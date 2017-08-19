package com.github.al.roulette.game.impl

import java.time.Duration
import java.util.UUID

import akka.{Done, NotUsed}
import com.github.al.roulette.game.api.{Game, GameService}
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class GameServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {
  private final val Uuid = UUID.fromString("7e595fac-830e-44f1-b73e-f8fd60594ace")

  private lazy val server = ServiceTest.startServer(ServiceTest.defaultSetup) { ctx =>
    new GameApplication(ctx) with LocalServiceLocator
  }

  private lazy val client = server.serviceClient.implement[GameService]

  "The GameService" should {
    "return new game's uuid" in {
      client.createGame.invoke(Game(None, "Some new game", Duration.ofMinutes(30))) map {
        response => response should ===(Uuid)
      }
    }

    "terminate game by id" in {
      client.terminateGame(Uuid).invoke(NotUsed) map {
        response => response should ===(Done)
      }
    }
  }

  override protected def beforeAll(): Unit = server

  override protected def afterAll() = server.stop()
}
