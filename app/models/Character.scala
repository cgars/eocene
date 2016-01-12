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

import play.api._
import play.api.mvc._
import play.api.db._
import play.api.Play.current
import anorm._
import play.api.db.DB
import eoceneServices.eoceneSqlStrings
import play.api.libs.json._
import java.sql.Connection
import eoceneServices.utilities
import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc.{ Action, RequestHeader }
import scala.math.max
import eoceneServices.EoceneUser
import eoceneServices.eoceneUserService
import eoceneServices.eoceneDao
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException

/**
 * A Character
 */
case class Character(val id: Int, val name: String, val dex_mod: Int, val str_mod: Int,
  val cha_mod: Int, val tou_mod: Int, val wil_mod: Int, val per_mod: Int, val dex_level: Int,
  val str_level: Int, val cha_level: Int, val tou_level: Int,
  val will_level: Int, val per_level: Int, val lp_av: Int, val lp_sp: Int,
  val kar_curr: Int, val pp: Int, val race: Race, val disciplines: List[Discipline],
  val talents: List[Talent], val spells: List[Spell], val skills: List[Skill],
  val armors: List[Armor], val derived: Map[String, Any]) {

  /**
   * Get the Dice for this Talent
   *
   * @param step
   * @return A string indicating the dices to be used
   */
  def getDice(step: Int) = eoceneServices.utilities.getDiceForStep(step)

  /**
   * Get a validator object for this character
   *
   * @return A Validator for this character
   */
  
  def getValidator: eoceneServices.Validator = {
    eoceneServices.Validator.getValidator(this)
  }

  /**
   * Get the sum of modification this character receives due to modiers
   *
   * @param name -> of the modfier ("physDef", "spellDef", "karmaMax", "ini"
   * 								  "socDef", "rec"->6)
   *
   * @return The summed modiefiers
   */
  
  def getModifierValueByName(name: String) = {
    disciplines.map(disci => disci.getModifierValueByName(name))
      .reduceOption((a1, a2) => max(a1, a2))
  }
}

/**
 * This Object is not only an Char factory but also the central DAL Object
 * for eocene Chars
 */
object Character {
  implicit val ChracterWrites = new Writes[Character] {
    def writes(char: Character) = JsObject(Seq(
      "id" -> JsNumber(char.id),
      "name" -> JsString(char.name),
      "dex" -> JsNumber(char.dex_mod),
      "str" -> JsNumber(char.str_mod),
      "cha" -> JsNumber(char.cha_mod),
      "tou" -> JsNumber(char.tou_mod),
      "will" -> JsNumber(char.wil_mod),
      "per" -> JsNumber(char.per_mod),
      "dex_level" -> JsNumber(char.dex_level),
      "str_level" -> JsNumber(char.str_level),
      "cha_level" -> JsNumber(char.cha_level),
      "tou_level" -> JsNumber(char.tou_level),
      "will_level" -> JsNumber(char.will_level),
      "per_level" -> JsNumber(char.per_level),
      "lp_av" -> JsNumber(char.lp_av),
      "kar_curr" -> JsNumber(char.kar_curr),
      "pp" -> JsNumber(char.pp),
      "Race" -> Json.toJson(char.race),
      "Disciplines" -> Json.toJson(char.disciplines),
      "Talents" -> Json.toJson(char.talents),
      "Skills" -> Json.toJson(char.skills),
      "Spells" -> Json.toJson(char.spells)))
  }

}
