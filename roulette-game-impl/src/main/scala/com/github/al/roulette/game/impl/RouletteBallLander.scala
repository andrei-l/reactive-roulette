package com.github.al.roulette.game.impl

import scala.util.Random

class RouletteBallLander {
  private final val WinningNumbers = Stream.continually(Random.nextInt(37)).iterator

  def landBall(): Int = WinningNumbers.next()
}
