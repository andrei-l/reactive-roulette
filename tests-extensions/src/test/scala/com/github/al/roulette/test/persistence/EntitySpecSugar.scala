package com.github.al.roulette.test.persistence

import akka.actor.ActorSystem
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.Matchers

import scala.language.{higherKinds, reflectiveCalls}

trait EntitySpecSugar extends Matchers {
  type P <: PersistentEntity

  def persistenceEntity: P {type Command = P#Command; type Event = P#Event; type State = P#State}

  def persistenceEntityId: String

  def withDriver[T](block: PersistentEntityTestDriver[P#Command, P#Event, P#State] => T)
                   (implicit system: ActorSystem): T = {
    val driver = new PersistentEntityTestDriver(system, persistenceEntity, persistenceEntityId)
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }

}
