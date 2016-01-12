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
import eoceneServices.eoceneSqlStrings

case class Race(var id: Int, var name: String, var dex_mod: Int, var str_mod: Int,
  var cha_mod: Int, var tou_mod: Int, var wil_mod: Int, var per_mod: Int, var kar_step: Int,
  var kar_start: Int, var kar_max: Int, var kar_cost: Int, var movement: Int,
  var abilities: String, var social_def: Int, var spell_def: Int,
  var rec_test: Int, var phys_armor: Int, var wound_thresh: Int, 
  var phys_def: Int)

object Race {

  implicit val raceWrites = new Writes[Race] {
    def writes(race: Race) = JsObject(Seq("id" -> JsNumber(race.id),
      "name" -> JsString(race.name),
      "dex" -> JsNumber(race.dex_mod),
      "str" -> JsNumber(race.str_mod),
      "cha" -> JsNumber(race.cha_mod),
      "tou" -> JsNumber(race.tou_mod),
      "will" -> JsNumber(race.wil_mod),
      "per" -> JsNumber(race.per_mod),
      "kar_step" -> JsNumber(race.kar_step),
      "kar_max" -> JsNumber(race.kar_max),
      "kar_cost" -> JsNumber(race.kar_cost),
      "movement" -> JsNumber(race.movement),
      "abilities" -> JsString(race.abilities),
      "social_def" -> JsNumber(race.social_def),
      "spell_def" -> JsNumber(race.spell_def),
      "phys_armor" -> JsNumber(race.phys_armor),
      "rec_test" -> JsNumber(race.rec_test),
      "wound_thresh" -> JsNumber(race.wound_thresh)))
  }

}
