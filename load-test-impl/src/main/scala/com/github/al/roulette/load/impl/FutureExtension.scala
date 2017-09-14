package com.github.al.roulette.load.impl

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

private[impl] case class FutureExtension[T](future: Future[T])(implicit executionContext: ExecutionContext) {
  def toFutureTry: Future[Try[T]] = future.map(Success(_)).recover({ case e => Failure(e) })

  def getSuccessfulFutures[V](onEach: => Unit)(implicit ev: T <:< IndexedSeq[Try[V]]): Future[IndexedSeq[V]] =
    future.map(_.collect({ case Success(x) => onEach; x }))

  def forAllFailureFutures[V](onEach: String => Unit)(implicit ev: T <:< IndexedSeq[Try[V]]): Unit =
    future.foreach(_.collect({ case Failure(e) => onEach(e.getMessage) }))

}

private[impl] object FutureExtension {
  implicit def futureToFutureExtension[T](future: Future[T])(implicit executionContext: ExecutionContext): FutureExtension[T] =
    new FutureExtension(future)
}
