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


import play.api.libs.json._

import scala.math.max


/**
 * A Character
 */
case class Character(val id: Int, val name: String, val dexMod: Int, val strMod: Int,
                     val chaMod: Int, val touMod: Int, val wilMod: Int, val perMod: Int, val dexLevel: Int,
                     val strLevel: Int, val chaLevel: Int, val touLevel: Int,
                     val willLevel: Int, val perLevel: Int, val lpAv: Int, val lpSp: Int,
                     val karCurr: Int, val pp: Int, val race: Race, val disciplines: List[Discipline],
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
  implicit val chracterWrites = new Writes[Character] {
    def writes(char: Character) = JsObject(Seq(
      "id" -> JsNumber(char.id),
      "name" -> JsString(char.name),
      "dex" -> JsNumber(char.dexMod),
      "str" -> JsNumber(char.strMod),
      "cha" -> JsNumber(char.chaMod),
      "tou" -> JsNumber(char.touMod),
      "will" -> JsNumber(char.wilMod),
      "per" -> JsNumber(char.perMod),
      "dex_level" -> JsNumber(char.dexLevel),
      "str_level" -> JsNumber(char.strLevel),
      "cha_level" -> JsNumber(char.chaLevel),
      "tou_level" -> JsNumber(char.touLevel),
      "will_level" -> JsNumber(char.willLevel),
      "per_level" -> JsNumber(char.perLevel),
      "lp_av" -> JsNumber(char.lpAv),
      "kar_curr" -> JsNumber(char.karCurr),
      "pp" -> JsNumber(char.pp),
      "Race" -> Json.toJson(char.race),
      "Disciplines" -> Json.toJson(char.disciplines),
      "Talents" -> Json.toJson(char.talents),
      "Skills" -> Json.toJson(char.skills),
      "Spells" -> Json.toJson(char.spells)))
  }

}
