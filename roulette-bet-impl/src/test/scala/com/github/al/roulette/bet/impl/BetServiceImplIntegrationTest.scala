package com.github.al.roulette.bet.impl

import java.time.Duration

import akka.http.javadsl.model.headers
import com.github.al.authentication.JwtTokenUtil
import com.github.al.persistence.UUIDConversions
import com.github.al.roulette.bet.BetComponents
import com.github.al.roulette.bet.api.{Bet, BetService}
import com.github.al.roulette.game.api.{GameEvent, GameService}
import com.github.al.roulette.{bet, game}
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LocalServiceLocator}
import com.lightbend.lagom.scaladsl.testkit.{ProducerStub, ProducerStubFactory, ServiceTest, TestTopicComponents}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers, Succeeded}
import play.api.libs.ws.ahc.AhcWSComponents

class BetServiceImplIntegrationTest
  extends AsyncWordSpec
    with Matchers with BeforeAndAfterAll with MockitoSugar with UUIDConversions {
  private final val GameDuration = Duration.ofMinutes(30)
  private final val GameId = "f1f2581e-880e-4a67-ba1d-1d8835243fdd"
  private final val PlayerId = "7f06847c-5ae1-470a-8068-7c24fb16be7e"
  private final val SampleBet = Bet(Some(2), bet.api.Number, 34.32)

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

      gameEventsProducerStub.send(game.api.GameStarted(GameId))
      for {
        _ <- placeBet
      } yield {
        Succeeded
      }
    }


  }

  private val jwtAuthorizationHeader = headers.Authorization.oauth2(JwtTokenUtil.createJwtToken("playerId", PlayerId))

  private def placeBet = betService
    .placeBet(GameId)
    .handleRequestHeader(header => header.addHeader(jwtAuthorizationHeader.name(), jwtAuthorizationHeader.value()))
    .invoke(SampleBet)

  override protected def afterAll(): Unit = server.stop()
}
