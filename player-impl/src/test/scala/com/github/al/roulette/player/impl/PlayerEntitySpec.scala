package com.github.al.roulette.player.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.github.al.roulette.player.PlayerSerializerRegistry
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver.Reply
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

class PlayerEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues {
  private final val PlayerId = "0a55e9c8-01e6-4c9c-b5c5-96f304dd0d0a"
  private final val PlayerName = "Some player"
  private final val SamplePlayerState = PlayerState(PlayerName)

  private val system = ActorSystem("test", JsonSerializerRegistry.actorSystemSetupFor(PlayerSerializerRegistry))

  "The player entity" should {
    "allow creating a player" in withDriver { driver =>
      val outcome = driver.run(CreatePlayer(SamplePlayerState))
      outcome.events should contain only PlayerCreated(SamplePlayerState)
      outcome.state should ===(Some(SamplePlayerState))
      outcome.sideEffects should contain only Reply(Done)
    }

    "allow creating and retrieving a player" in withDriver { driver =>
      val outcome = driver.run(CreatePlayer(SamplePlayerState), GetPlayer)
      outcome.events should contain only PlayerCreated(SamplePlayerState)
      outcome.state should ===(Some(SamplePlayerState))
      outcome.sideEffects should contain theSameElementsInOrderAs Seq(Reply(Done), Reply(Some(SamplePlayerState)))
    }
  }

  private def withDriver[T](block: PersistentEntityTestDriver[PlayerCommand, PlayerEvent, Option[PlayerState]] => T): T = {
    val driver = new PersistentEntityTestDriver(system, new PlayerEntity, PlayerId)
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }


  protected override def afterAll: Unit = TestKit.shutdownActorSystem(system)
}
