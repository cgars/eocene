/*
 * Copyright (c) 2016 Christian Garbers.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Simplified BSD License
 *  which accompanies this distribution
 *  Contributors:
 *       Christian Garbers - initial API and implementation
 */
import play.api._

object Global extends GlobalSettings {

  //  /**
  //   * The runtime environment for this app.
  //   */
  //  object MyRuntimeEnvironment extends RuntimeEnvironment.Default[EoceneUser] {
  //    //override lazy val routes = new CustomRoutesService()
  //    //override lazy val userService: InMemoryUserService = new InMemoryUserService()
  //    override lazy val userService: eoceneServices.eoceneUserService =
  //      new eoceneServices.eoceneUserService()
  //    override lazy val providers = ListMap(
  //      include(new FacebookProvider(routes, cacheService,
  //        oauth2ClientFor(FacebookProvider.Facebook))),
  //      include(new GoogleProvider(routes, cacheService,
  //        oauth2ClientFor(GoogleProvider.Google))))
  //  }
  //  val dao = new eoceneSqlService

}
