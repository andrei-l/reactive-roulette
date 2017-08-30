package com.github.al.roulette.game.impl

import java.time.Duration
import java.util.UUID

import akka.stream.scaladsl.Sink
import com.github.al.roulette.game.api.{Game, GameService}
import com.github.al.roulette.game.{GameComponents, api}
import com.github.al.roulette.scheduler
import com.github.al.roulette.scheduler.api.{GameSchedulerService, ScheduledGameEvent}
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LocalServiceLocator}
import com.lightbend.lagom.scaladsl.testkit.{ProducerStub, ProducerStubFactory, ServiceTest, TestTopicComponents}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable.Seq
import scala.language.postfixOps

class GameServiceImplIntegrationTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll with MockitoSugar {
  private final val GameDuration = Duration.ofMinutes(30)
  private final val SampleGame = Game("Some new game", GameDuration)
  private final val SampleWinningNumber = 14
  private val mockGameSchedulerService = mock[GameSchedulerService]
  private val mockRouletteBallLander = mock[RouletteBallLander]
  private var scheduledGameEventsProducerStub: ProducerStub[ScheduledGameEvent] = _


  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with GameComponents with LocalServiceLocator with AhcWSComponents with TestTopicComponents {
      val stubFactory = new ProducerStubFactory(actorSystem, materializer)
      scheduledGameEventsProducerStub = stubFactory.producer[ScheduledGameEvent](GameSchedulerService.ScheduledGameEventTopicName)

      when(mockGameSchedulerService.scheduledEvents).thenReturn(scheduledGameEventsProducerStub.topic)
      when(mockRouletteBallLander.landBall()).thenReturn(SampleWinningNumber)

      override lazy val gameSchedulerService: GameSchedulerService = mockGameSchedulerService
      override lazy val rouletteBallLander: RouletteBallLander = mockRouletteBallLander
    }
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

    "emit game create, started & finished events" in {
      import server.materializer
      server.application.scheduledEventsSubscriber

      for {
        createdGameId <- createSampleGame
        gameId = createdGameId.gameId
        _ = scheduledGameEventsProducerStub.send(scheduler.api.GameStarted(gameId))
        _ = scheduledGameEventsProducerStub.send(scheduler.api.GameFinished(gameId))
        gameEvents <- gameService.gameEvents.subscribe.atMostOnceSource
          .filter(_.gameId == gameId)
          .take(3)
          .runWith(Sink.seq)
        gameResultsEvents <- gameService.gameResultEvents.subscribe.atMostOnceSource
          .filter(_.gameId == gameId)
          .take(1)
          .runWith(Sink.seq)
      } yield {
        gameEvents should contain allOf(
          api.GameCreated(gameId, GameDuration),
          api.GameStarted(gameId),
          api.GameFinished(gameId)
        )
        gameResultsEvents.head shouldBe api.GameResulted(gameId, SampleWinningNumber)
      }
    }
  }


  private def getGame(gameId: UUID) = gameService.getGame(gameId).invoke

  private def createSampleGame = gameService.createGame.invoke(SampleGame)

  override protected def afterAll(): Unit = server.stop()
}
