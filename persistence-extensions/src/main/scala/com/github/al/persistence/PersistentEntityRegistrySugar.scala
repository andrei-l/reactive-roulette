package com.github.al.persistence

import java.util.UUID

import com.lightbend.lagom.scaladsl.persistence.{PersistentEntity, PersistentEntityRef, PersistentEntityRegistry}

import scala.reflect.ClassTag

trait PersistentEntityRegistrySugar {
  val entityRegistry: PersistentEntityRegistry

  def entityRefUuid[P <: PersistentEntity : ClassTag](id: UUID): PersistentEntityRef[P#Command] = entityRefString(id.toString)

  def entityRefString[P <: PersistentEntity : ClassTag](id: String): PersistentEntityRef[P#Command] = entityRegistry.refFor[P](id)


}
