/*******************************************************************************
 * Copyright (c) 2014 Christian Garbers.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Simplified BSD License
 * which accompanies this distribution
 *
 * Contributors:
 *     Christian Garbers - initial API and implementation
 * ****************************************************************************
 */
import play.api._
import java.lang.reflect.Constructor
import securesocial.core.RuntimeEnvironment
import eoceneServices.{ EoceneUser }
import securesocial.core.providers._
import scala.collection.immutable.ListMap

object Global extends GlobalSettings {

  /**
   * The runtime environment for this app.
   */
  object MyRuntimeEnvironment extends RuntimeEnvironment.Default[EoceneUser] {
    //override lazy val routes = new CustomRoutesService()
    //override lazy val userService: InMemoryUserService = new InMemoryUserService()
    override lazy val userService: eoceneServices.eoceneUserService =
      new eoceneServices.eoceneUserService()
    override lazy val providers = ListMap(
      include(new FacebookProvider(routes, cacheService,
        oauth2ClientFor(FacebookProvider.Facebook))),
      include(new GoogleProvider(routes, cacheService,
        oauth2ClientFor(GoogleProvider.Google))))
  }

  /**
   * An implementation that checks if the controller expects a RuntimeEnvironment and
   * passes the instance to it if required.
   *
   * This is copied from the examples
   *
   * @param controllerClass
   * @tparam A
   * @return
   */
  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    val instance = controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[RuntimeEnvironment[EoceneUser]]
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(MyRuntimeEnvironment)
    }
    instance.getOrElse(super.getControllerInstance(controllerClass))
  }
}
