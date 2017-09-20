package com.github.al.roulette.player.impl

import java.util.UUID

import akka.stream.scaladsl.Sink
import com.github.al.authentication.JwtTokenUtil
import com.github.al.persistence.UUIDConversions
import com.github.al.roulette.player.api.{Player, PlayerAccessToken, PlayerCredentials, PlayerService}
import com.github.al.roulette.player.{PlayerComponents, api}
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LocalServiceLocator}
import com.lightbend.lagom.scaladsl.testkit.{ServiceTest, TestTopicComponents}
import org.scalatest._
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.language.postfixOps

class PlayerServiceImplIntegrationTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll with UUIDConversions {
  private final val SamplePlayer = Player("some name")

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with PlayerComponents with LocalServiceLocator with AhcWSComponents with TestTopicComponents
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

      val a = (0 to 100 par) map { i =>
        val newPlayer = Player(s"some new name # $i")

        for {
          createdPlayerId <- createPlayer(newPlayer)
          events: Seq[api.PlayerEvent] <- playerService.playerEvents.subscribe.atMostOnceSource
            .filter(_.playerId == createdPlayerId.playerId)
            .take(1)
            .runWith(Sink.seq)
          playerAccessToken <- login(newPlayer)
        } yield {
          events.size shouldBe 1
          events.head shouldBe an[api.PlayerRegistered]
          playerAccessToken should matchPattern { case PlayerAccessToken(token) if isValidAccessToken(token, createdPlayerId.playerId) => }
        }
      }
      a.fold[Future[Assertion]](Future.successful(Succeeded))({ case (f1, f2) => f1.flatMap(_ => f2) })
    }
  }


  private def getPlayer(playerId: UUID) = playerService.getPlayer(playerId).invoke

  private def createPlayer(player: Player = SamplePlayer) = playerService.registerPlayer.invoke(player)

  private def login(player: Player = SamplePlayer) = playerService.login.invoke(PlayerCredentials(player.playerName))

  private def isValidAccessToken(token: String, playerId: String) =
    JwtTokenUtil.extractPayloadField(token, "playerId").contains(playerId)

  override protected def afterAll(): Unit = server.stop()
}
