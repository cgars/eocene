/*
 * Copyright (c) 2016 Christian Garbers.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Simplified BSD License
 *  which accompanies this distribution
 *  Contributors:
 *       Christian Garbers - initial API and implementation
 */


import com.google.inject.{AbstractModule, TypeLiteral}
import eoceneServices.{eoceneDao, eoceneEnvironment, eoceneSqlService}
import net.codingwell.scalaguice.ScalaModule
import securesocial.core.RuntimeEnvironment

class EoceneModule extends AbstractModule with ScalaModule {
  override def configure() {
    val environment: eoceneEnvironment = new eoceneEnvironment
    bind(new TypeLiteral[RuntimeEnvironment] {}).toInstance(environment)
    val dao: eoceneSqlService = new eoceneSqlService
    bind(new TypeLiteral[eoceneDao]() {}).toInstance(dao)
  }
}