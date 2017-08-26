package com.github.al.roulette.game.impl

import java.time.Duration
import java.util.UUID

import akka.stream.scaladsl.Sink
import akka.{Done, NotUsed}
import com.github.al.roulette.game.api.{Game, GameService}
import com.github.al.roulette.game.{GameComponents, api}
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LocalServiceLocator}
import com.lightbend.lagom.scaladsl.testkit.{ServiceTest, TestTopicComponents}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable.Seq

class GameServiceImplIntegrationTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {
  private final val GameId = UUID.fromString("7e595fac-830e-44f1-b73e-f8fd60594ace")
  private final val SampleGame = Game("Some new game", Duration.ofMinutes(30))

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with GameComponents with LocalServiceLocator with AhcWSComponents with TestTopicComponents
  }

  private val gameService = server.serviceClient.implement[GameService]


  "The GameService" should {
    "allow game creation" in {
      for {
        createdGameId <- createSampleGame
        retrieved <- getGame(createdGameId.gameId)
      } yield {
        SampleGame should ===(retrieved)
      }
    }


    "emit game created event" in {
      import server.materializer

      for {
        createdGameId <- createSampleGame
        events: Seq[api.GameEvent] <- gameService.gameEvents.subscribe.atMostOnceSource
          .filter(_.gameId == createdGameId.gameId)
          .take(1)
          .runWith(Sink.seq)
      } yield {
        events.size shouldBe 1
        events.head shouldBe an[api.GameCreated]
      }
    }

    "terminate game by id" in {
      gameService.terminateGame(GameId).invoke(NotUsed) map {
        response => response should ===(Done)
      }
    }
  }


  private def getGame(gameId: UUID) = gameService.getGame(gameId).invoke

  private def createSampleGame = gameService.createGame.invoke(SampleGame)

  override protected def afterAll(): Unit = server.stop()
}
