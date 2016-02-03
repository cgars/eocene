/*
 * Copyright (c) 2016 Christian Garbers.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Simplified BSD License
 *  which accompanies this distribution
 *  Contributors:
 *       Christian Garbers - initial API and implementation
 */

package eoceneServices

import securesocial.core.providers._

import scala.collection.immutable.ListMap

//import controllers.CustomRoutesService

import securesocial.core.RuntimeEnvironment

class eoceneEnvironment extends RuntimeEnvironment.Default {
  override type U = EoceneUser
  //override lazy val routes = new CustomRoutesService()
  override lazy val userService: eoceneUserService = new eoceneUserService()
  override lazy val providers = ListMap(
    include(new FacebookProvider(routes, cacheService,
      oauth2ClientFor(FacebookProvider.Facebook))),
    include(new GoogleProvider(routes, cacheService,
      oauth2ClientFor(GoogleProvider.Google))))
  override implicit val executionContext = play.api.libs.concurrent.Execution.defaultContext
}
