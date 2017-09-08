package com.github.al.persistence

import java.util.UUID

import com.lightbend.lagom.scaladsl.persistence.{PersistentEntity, PersistentEntityRef, PersistentEntityRegistry}

import scala.language.implicitConversions
import scala.reflect.ClassTag

trait PersistentEntityRegistrySugar extends UUIDConversions {
  val entityRegistry: PersistentEntityRegistry

  def entityRef[P <: PersistentEntity : ClassTag](id: UUID): PersistentEntityRef[P#Command] = entityRegistry.refFor[P](id)
}
