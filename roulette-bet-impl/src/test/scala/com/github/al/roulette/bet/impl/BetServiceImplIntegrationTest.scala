package com.github.al.roulette.bet.impl

import java.util.UUID

import akka.http.javadsl.model.headers
import akka.stream.scaladsl.Sink
import com.github.al.authentication.JwtTokenUtil
import com.github.al.persistence.UUIDConversions
import com.github.al.roulette.bet.BetComponents
import com.github.al.roulette.bet.api.{AllGameBetsProclaimed, Bet, BetService, PlayerBets}
import com.github.al.roulette.game.api.{GameEvent, GameService}
import com.github.al.roulette.{bet, game}
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LocalServiceLocator}
import com.lightbend.lagom.scaladsl.testkit.{ProducerStub, ProducerStubFactory, ServiceTest, TestTopicComponents}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers, Succeeded}
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.Future

class BetServiceImplIntegrationTest
  extends AsyncWordSpec
    with Matchers with BeforeAndAfterAll with MockitoSugar with UUIDConversions {
  private final val GameId = "f1f2581e-880e-4a67-ba1d-1d8835243fdd"
  private final val GameId2 = "67ecf189-6caa-4963-b87f-31e162ea22da"
  private final val GameId3 = "f53359db-c8ae-4af1-a738-954aaa58d027"
  private final val GameIdUUID: UUID = GameId
  private final val GameId2UUID: UUID = GameId2
  private final val PlayerId = "7f06847c-5ae1-470a-8068-7c24fb16be7e"
  private final val PlayerId2 = "18f69c13-675f-4202-8d33-ea62120fafd0"
  private final val SampleBet = Bet(Some(2), bet.api.Number, 34.32)

  private final val ExpectedPlayerBets = PlayerBets(PlayerId, List(SampleBet, SampleBet))
  private final val ExpectedPlayerBets2 = PlayerBets(PlayerId2, List(SampleBet))

  private val mockGameService = mock[GameService]
  private var gameEventsProducerStub: ProducerStub[GameEvent] = _

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with BetComponents with LocalServiceLocator with AhcWSComponents with TestTopicComponents {
      val stubFactory = new ProducerStubFactory(actorSystem, materializer)
      gameEventsProducerStub = stubFactory.producer[GameEvent](GameService.GameEventTopicName)

      when(mockGameService.gameEvents).thenReturn(gameEventsProducerStub.topic)
      override lazy val gameService: GameService = mockGameService
    }
  }

  private val betService = server.serviceClient.implement[BetService]


  "The BetService" should {
    "allow placing bets" in {
      server.application.gameEventsSubscriber

      gameEventsProducerStub.send(game.api.GameStarted(GameId3))
      for {
        _ <- placeBet(GameId3, PlayerId)
      } yield {
        Succeeded
      }
    }

    "emit AllGameBetsProclaimed event with no bets" in {
      server.application.gameEventsSubscriber
      import server.materializer

      gameEventsProducerStub.send(game.api.GameStarted(GameId))
      gameEventsProducerStub.send(game.api.GameFinished(GameId))
      for {
        rouletteBetsEvents <- betService.rouletteBetsEvents.subscribe.atMostOnceSource
          .filter(_.gameId == GameIdUUID)
          .take(1)
          .runWith(Sink.seq)
      } yield {
        rouletteBetsEvents.head shouldBe AllGameBetsProclaimed(GameId, Nil)
      }
    }

    "emit AllGameBetsProclaimed event with bets" in {
      server.application.gameEventsSubscriber
      import server.materializer

      gameEventsProducerStub.send(game.api.GameStarted(GameId2))
      for {
        _ <- placeBet(GameId2, PlayerId)
        _ <- placeBet(GameId2, PlayerId2)
        _ <- placeBet(GameId2, PlayerId)
        _ = gameEventsProducerStub.send(game.api.GameFinished(GameId2))
        rouletteBetsEvents <- betService.rouletteBetsEvents.subscribe.atMostOnceSource
          .filter(_.gameId == GameId2UUID)
          .take(1)
          .runWith(Sink.seq)
      } yield {
        rouletteBetsEvents.head should matchPattern {
          case AllGameBetsProclaimed(GameId2UUID, playerBets)
            if playerBets.size == 2 && playerBets.toSet.subsetOf(Set(ExpectedPlayerBets, ExpectedPlayerBets2)) =>
        }
      }
    }
  }

  private def placeBet(gameId: String, playerId: String): Future[_] = {
    val jwtAuthorizationHeader = buildJwtAuthorizationHeader(playerId)
    betService
      .placeBet(gameId)
      .handleRequestHeader(header => header.addHeader(jwtAuthorizationHeader.name(), jwtAuthorizationHeader.value()))
      .invoke(SampleBet)
  }

  private def buildJwtAuthorizationHeader(playerId: String) =
    headers.Authorization.oauth2(JwtTokenUtil.createJwtToken("playerId", playerId))

  override protected def afterAll(): Unit = server.stop()
}
