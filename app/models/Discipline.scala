/**
 * *****************************************************************************
 * Copyright (c) 2014 Christian Garbers.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Simplified BSD License
 * which accompanies this distribution
 *
 * Contributors:
 *     Christian Garbers - initial API and implementation
 * ****************************************************************************
 */
package models

import play.api.libs.json.Format
import play.api.libs.json._
import eoceneServices.eoceneSqlStrings
import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api._
/**
 * Discipline
 */
case class Discipline(val id: Int, val name: String, val abilities: String,
  val circle: Option[Int], val modifiers: List[Modifier]) {

  /**
   * Get the sum of modification this discipline receives due to a modifier
   *
   * @param name -> of the modfier ("physDef", "spellDef", "karmaMax", "ini"
   * 								  "socDef", "rec"->6)
   *
   * @return The summed modiefiers
   */
  def getModifierValueByName(name: String): Int = {
    modifiers.filter(mod => mod.id == Modifier.modfierName2Id(name))
      .map(mod => mod.value)
      .reduceOption((a1, a2) => a1 + a2).getOrElse(0)
  }
}

object Discipline {
  implicit val DisciplineWrites = new Writes[Discipline] {
    def writes(discipline: Discipline) = discipline.circle match {
      case None => JsObject(Seq("id" -> JsNumber(discipline.id),
        "name" -> JsString(discipline.name),
        "Abilities" -> JsString(discipline.abilities),
        "circle" -> JsNumber(0)))
      case _ => JsObject(Seq("id" -> JsNumber(discipline.id),
        "name" -> JsString(discipline.name),
        "Abilities" -> JsString(discipline.abilities),
        "circle" -> JsNumber(discipline.circle.get)))
    }
  }
}
