package com.github.al.roulette.player.impl

import java.util.UUID

import akka.stream.scaladsl.Sink
import com.github.al.roulette.player.api.{Player, PlayerService}
import com.github.al.roulette.player.{PlayerComponents, api}
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LocalServiceLocator}
import com.lightbend.lagom.scaladsl.testkit.{ServiceTest, TestTopicComponents}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable.Seq

class PlayerServiceImplIntegrationTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {
  private final val SamplePlayer = Player("some name")

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with PlayerComponents with LocalServiceLocator with AhcWSComponents with TestTopicComponents
  }

  private val playerService = server.serviceClient.implement[PlayerService]


  "The PlayerService" should {
    "allow player creation & fetching" in {

      for {
        createdPlayerId <- createSamplePlayer
        retrieved <- getPlayer(createdPlayerId.playerId)
      } yield {
        SamplePlayer should ===(retrieved)
      }
    }

    "emit player created event" in {
      import server.materializer

      for {
        createdPlayerId <- createSamplePlayer
        events: Seq[api.PlayerEvent] <- playerService.playerEvents.subscribe.atMostOnceSource
          .filter(_.playerId == createdPlayerId.playerId)
          .take(1)
          .runWith(Sink.seq)
      } yield {
        events.size shouldBe 1
        events.head shouldBe an[api.PlayerRegistered]
      }
    }
  }


  private def getPlayer(playerId: UUID) = playerService.getPlayer(playerId).invoke

  private def createSamplePlayer = playerService.registerPlayer.invoke(SamplePlayer)

  override protected def afterAll(): Unit = server.stop()

}
