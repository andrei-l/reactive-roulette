package com.github.al.roulette.player.impl

import java.util.UUID

import akka.stream.scaladsl.Sink
import com.github.al.authentication.JwtTokenUtil
import com.github.al.persistence.UUIDConversions
import com.github.al.roulette.player.api.{Player, PlayerCredentials, PlayerService}
import com.github.al.roulette.player.{PlayerComponents, api}
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LocalServiceLocator}
import com.lightbend.lagom.scaladsl.testkit.{ServiceTest, TestTopicComponents}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable.Seq

class PlayerServiceImplIntegrationTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll with UUIDConversions {
  private final val SamplePlayer = Player("some name")

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with PlayerComponents with LocalServiceLocator with AhcWSComponents with TestTopicComponents {
      override def additionalConfiguration: AdditionalConfiguration =
        super.additionalConfiguration ++ Configuration.from(Map(
          "cassandra-query-journal.eventual-consistency-delay" -> "0"
        ))
    }
  }

  private val playerService = server.serviceClient.implement[PlayerService]


  "The PlayerService" should {
    "allow player creation & fetching" in {

      for {
        createdPlayerId <- createPlayer()
        retrieved <- getPlayer(createdPlayerId.playerId)
      } yield {
        SamplePlayer should ===(retrieved)
      }
    }

    "emit player created event" in {
      import server.materializer

      for {
        createdPlayerId <- createPlayer()
        events: Seq[api.PlayerEvent] <- playerService.playerEvents.subscribe.atMostOnceSource
          .filter(_.playerId == createdPlayerId.playerId)
          .take(1)
          .runWith(Sink.seq)
      } yield {
        events.size shouldBe 1
        events.head shouldBe an[api.PlayerRegistered]
      }
    }

    "login user by providing access token" in {
      import server.materializer

      val samplePlayer2 = Player("some new name")

      for {
        createdPlayerId <- createPlayer(samplePlayer2)
        events: Seq[api.PlayerEvent] <- playerService.playerEvents.subscribe.atMostOnceSource
          .filter(_.playerId == createdPlayerId.playerId)
          .take(1)
          .runWith(Sink.seq)
        playerAccessToken <- login(samplePlayer2)
        if isValidAccessToken(playerAccessToken.token, createdPlayerId.playerId)
      } yield {
        events.size shouldBe 1
        events.head shouldBe an[api.PlayerRegistered]
      }
    }
  }


  private def getPlayer(playerId: UUID) = playerService.getPlayer(playerId).invoke

  private def createPlayer(player: Player = SamplePlayer) = playerService.registerPlayer.invoke(player)

  private def login(player: Player = SamplePlayer) = playerService.login.invoke(PlayerCredentials(player.playerName))

  private def isValidAccessToken(token: String, playerId: String) =
    JwtTokenUtil.extractPayloadField(token, "playerId").contains(playerId)

  override protected def afterAll(): Unit = server.stop()
}
