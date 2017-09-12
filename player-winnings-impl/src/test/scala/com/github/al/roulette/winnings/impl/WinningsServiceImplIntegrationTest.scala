package com.github.al.roulette.winnings.impl

import java.util.UUID

import akka.persistence.query.NoOffset
import akka.stream.scaladsl.Sink
import com.github.al.persistence.UUIDConversions
import com.github.al.roulette.bet.api.{Bet, BetService, PlayerBets, RouletteBetsEvent}
import com.github.al.roulette.game.api.{GameResulted, GameService}
import com.github.al.roulette.{bet, game, winnings}
import com.github.al.roulette.winnings.WinningsComponents
import com.github.al.roulette.winnings.api.{PlayerWinning, WinningsService}
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LocalServiceLocator}
import com.lightbend.lagom.scaladsl.testkit.{ProducerStub, ProducerStubFactory, ServiceTest, TestTopicComponents}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class WinningsServiceImplIntegrationTest
  extends AsyncWordSpec
    with Matchers with BeforeAndAfterAll with MockitoSugar with UUIDConversions {
  private final val GameId = "df8f5eba-ec74-4df5-8326-aff31500f845"
  private final val GameId2 = "8f96b841-3929-4d06-b642-4099ff9d62a6"
  private final val GameId3 = "9814586c-83e4-4655-b383-21a77eeb0f1e"
  private final val GameId4: UUID = "972fea58-485d-4d32-8db6-bb63e505f909"
  private final val PlayerId: UUID = "695e66d7-4974-49f4-90fc-fed330472091"

  private final val Bets = PlayerBets(PlayerId, List(Bet(None, bet.api.Even, 2)))

  private val mockGameService = mock[GameService]
  private val mockBetService = mock[BetService]
  private var gameResultedEventsProducerStub: ProducerStub[GameResulted] = _
  private var rouletteBetsEventProducerStub: ProducerStub[RouletteBetsEvent] = _

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with WinningsComponents with LocalServiceLocator with AhcWSComponents with TestTopicComponents {
      val stubFactory = new ProducerStubFactory(actorSystem, materializer)
      gameResultedEventsProducerStub = stubFactory.producer[GameResulted](GameService.GameEventTopicName)
      rouletteBetsEventProducerStub = stubFactory.producer[RouletteBetsEvent](BetService.BetEventTopicName)

      when(mockGameService.gameResultEvents).thenReturn(gameResultedEventsProducerStub.topic)
      when(mockBetService.rouletteBetsEvents).thenReturn(rouletteBetsEventProducerStub.topic)
      override lazy val gameService: GameService = mockGameService
      override lazy val betService: BetService = mockBetService
    }
  }

  private val winningsService = server.serviceClient.implement[WinningsService]


  "The WinningsService" should {
    "allow saving winning number" in {
      server.application.gameResultEventsSubscriber
      import server.materializer

      gameResultedEventsProducerStub.send(game.api.GameResulted(GameId, 4))
      for {
        winningsEvents <- server.application.persistentEntityRegistry.eventStream(WinningsEvent.Tag, NoOffset)
          .filter(_.entityId == GameId)
          .map(_.event)
          .take(1)
          .runWith(Sink.seq)
      } yield {
        winningsEvents.head shouldBe GameResultSaved(4)
      }
    }

    "allow saving players bets" in {
      server.application.gameBetsProclamationEventsSubscriber
      import server.materializer

      rouletteBetsEventProducerStub.send(bet.api.AllGameBetsProclaimed(GameId2, List(Bets)))
      for {
        winningsEvents <- server.application.persistentEntityRegistry.eventStream(WinningsEvent.Tag, NoOffset)
          .filter(_.entityId == GameId2)
          .map(_.event)
          .take(1)
          .runWith(Sink.seq)
      } yield {
        winningsEvents.head shouldBe PlayersBetsSaved(List(Bets))
      }
    }

    "allow receiving WinningsCalculated event after winning number and players bets are saved" in {
      server.application.gameResultEventsSubscriber
      server.application.gameBetsProclamationEventsSubscriber
      import server.materializer

      rouletteBetsEventProducerStub.send(bet.api.AllGameBetsProclaimed(GameId3, List(Bets)))
      gameResultedEventsProducerStub.send(game.api.GameResulted(GameId3, 4))
      for {
        winningsEvents <- server.application.persistentEntityRegistry.eventStream(WinningsEvent.Tag, NoOffset)
          .filter(_.entityId == GameId3)
          .map(_.event)
          .take(3)
          .runWith(Sink.seq)
      } yield {
        winningsEvents should contain allElementsOf Seq(
          PlayersBetsSaved(List(Bets)),
          GameResultSaved(4),
          WinningsCalculated(List(PlayerWinning(PlayerId, 4)))
        )
      }
    }

    "allow receiving WinningsCalculated event after winning number and players bets are saved (topic check)" in {
      server.application.gameResultEventsSubscriber
      server.application.gameBetsProclamationEventsSubscriber
      import server.materializer


      server.actorSystem.scheduler.scheduleOnce(400 millis) {
        rouletteBetsEventProducerStub.send(bet.api.AllGameBetsProclaimed(GameId4, List(Bets)))
        gameResultedEventsProducerStub.send(game.api.GameResulted(GameId4, 4))
      }

      for {
        winningsEvents <- winningsService.winningsEvents.subscribe.atMostOnceSource
          .filter(_.gameId == GameId4)
          .take(1)
          .runWith(Sink.seq)
      } yield {
        winningsEvents should contain allElementsOf Seq(
          winnings.api.WinningsCalculated(GameId4, List(PlayerWinning(PlayerId, 4)))
        )
      }
    }
  }
}
