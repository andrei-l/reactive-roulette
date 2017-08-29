package com.github.al.roulette.scheduler.impl

import java.time.Duration
import java.util.UUID

import akka.stream.scaladsl.Sink
import com.github.al.roulette.game.api._
import com.github.al.roulette.scheduler.SchedulerComponents
import com.github.al.roulette.scheduler.api.{GameFinished, GameSchedulerService, GameStarted, ScheduledGameEvent}
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LocalServiceLocator}
import com.lightbend.lagom.scaladsl.testkit.{ProducerStub, ProducerStubFactory, ServiceTest, TestTopicComponents}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

import scala.collection.immutable.Seq
import scala.concurrent.duration.DurationDouble
import scala.language.postfixOps

class GameSchedulerServiceImplIntegrationTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll with MockitoSugar {
  private final val GameId = UUID.fromString("7e595fac-830e-44f1-b73e-f8fd60594ace")
  private val mockGameService = mock[GameService]
  private var gameEventsProducerStub: ProducerStub[GameEvent] = _

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with SchedulerComponents with LocalServiceLocator with TestTopicComponents {
      val stubFactory = new ProducerStubFactory(actorSystem, materializer)
      gameEventsProducerStub = stubFactory.producer[GameEvent](GameService.GameEventTopicName)

      when(mockGameService.gameEvents).thenReturn(gameEventsProducerStub.topic)
      override lazy val gameService: GameService = mockGameService
    }
  }


  private val schedulerService = server.serviceClient.implement[GameSchedulerService]


  "The GameSchedulerService" should {
    "emit game started event" in {
      import server.materializer

      server.application.gameScheduler
      server.actorSystem.scheduler.scheduleOnce(400 millis) {
        gameEventsProducerStub.send(GameCreated(GameId, Duration.ofMillis(50)))
      }
      for {
        events: Seq[ScheduledGameEvent] <- schedulerService.scheduledEvents.subscribe.atMostOnceSource
          .filter(_.gameId == GameId)
          .take(2)
          .runWith(Sink.seq)
      } yield {
        events.size shouldBe 2
        events.head shouldBe an[GameStarted]
        events.tail.head shouldBe an[GameFinished]
      }
    }
  }


  override protected def afterAll(): Unit = server.stop()
}
