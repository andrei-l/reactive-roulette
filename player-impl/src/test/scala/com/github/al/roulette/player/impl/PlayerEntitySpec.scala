package com.github.al.roulette.player.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.github.al.roulette.player.PlayerSerializerRegistry
import com.github.al.roulette.player.impl.PlayerAccessTokenValidator.isValidAccessToken
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

    "allow creating and issuing access token" in withDriver { driver =>
      val outcome = driver.run(CreatePlayer(SamplePlayerState), IssueAccessToken)
      outcome.events.size should ===(2)
      outcome.events.head should ===(PlayerCreated(SamplePlayerState))
      outcome.events.tail.head should matchPattern { case AccessTokenIssued(token) if isValidAccessToken(token, PlayerId) => }
      outcome.state should matchPattern { case Some(PlayerState(playerName, token :: Nil)) if playerName == PlayerName && isValidAccessToken(token, PlayerId) => }
      outcome.sideEffects.size should ===(2)
      outcome.sideEffects.head should ===(Reply(Done))
      outcome.sideEffects.tail.head should matchPattern { case Reply(token: String) if isValidAccessToken(token, PlayerId) => }
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
