/*
 * Copyright (c) 2016 Christian Garbers.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Simplified BSD License
 *  which accompanies this distribution
 *  Contributors:
 *       Christian Garbers - initial API and implementation
 */
package models

import play.api.libs.json._

case class Race(var id: Int, var name: String, var dexMod: Int, var strMod: Int,
                var chaMod: Int, var touMod: Int, var wilMod: Int, var perMod: Int, var karStep: Int,
                var karStart: Int, var karMax: Int, var karCost: Int, var movement: Int,
                var abilities: String, var socialDef: Int, var spellDef: Int,
                var recTest: Int, var physArmor: Int, var woundThresh: Int,
                var physDef: Int)

object Race {

  implicit val raceWrites = new Writes[Race] {
    def writes(race: Race) = JsObject(Seq("id" -> JsNumber(race.id),
      "name" -> JsString(race.name),
      "dex" -> JsNumber(race.dexMod),
      "str" -> JsNumber(race.strMod),
      "cha" -> JsNumber(race.chaMod),
      "tou" -> JsNumber(race.touMod),
      "will" -> JsNumber(race.wilMod),
      "per" -> JsNumber(race.perMod),
      "kar_step" -> JsNumber(race.karStep),
      "kar_max" -> JsNumber(race.karMax),
      "kar_cost" -> JsNumber(race.karCost),
      "movement" -> JsNumber(race.movement),
      "abilities" -> JsString(race.abilities),
      "social_def" -> JsNumber(race.socialDef),
      "spell_def" -> JsNumber(race.spellDef),
      "phys_armor" -> JsNumber(race.physArmor),
      "rec_test" -> JsNumber(race.recTest),
      "wound_thresh" -> JsNumber(race.woundThresh)))
  }

}
