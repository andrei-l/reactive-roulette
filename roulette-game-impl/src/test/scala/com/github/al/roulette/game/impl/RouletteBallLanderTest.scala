package com.github.al.roulette.game.impl

import org.scalacheck.Gen
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.prop.PropertyChecks

class RouletteBallLanderTest extends WordSpec with Matchers with PropertyChecks {
  private val rouletteBallLander = new RouletteBallLander

  private val landedBalls = Gen.resultOf[Unit, Int]((_) => rouletteBallLander.landBall())

  "The roulette ball lander" should {
    "emit only values from 0-36 range" in {
      forAll(landedBalls) { landedBall: Int =>
        landedBall should (be >= 0 and be <= 36)
      }
    }

    "emit all values from 0-36 range" in {
      var requiredNumbers = (0 to 36).toSet

      forAll(landedBalls, minSuccessful(20000)) { landedBall: Int =>
        requiredNumbers -= landedBall
      }
      assert(requiredNumbers === Set())
    }
  }
}
