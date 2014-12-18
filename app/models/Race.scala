/*******************************************************************************
 * Copyright (c) 2014 Christian Garbers.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Simplified BSD License
 * which accompanies this distribution
 * 
 * Contributors:
 *     Christian Garbers - initial API and implementation
 ******************************************************************************/
package models

import play.api.libs.json.Format
import play.api.libs.json._
import eoceneServices.eoceneSqlStrings 
import anorm._
import play.api.db.DB
import play.api.Play.current

case class Race(var id: Int, var name: String, var dex_mod: Int, var str_mod: Int,
  var cha_mod: Int, var tou_mod: Int, var wil_mod: Int, var per_mod: Int, var kar_step: Int,
  var kar_start: Int, var kar_max: Int, var kar_cost: Int, var movement: Int,
  var abilities: String, var social_def: Int, var spell_def: Int,
  var rec_test: Int, var phys_armor: Int, var wound_thresh: Int)

object Race {
  
  /**
   * Get a Race build from row
   * 
   * @param rows the rows from a db call
   * @return Discipline
   */ 
  def getRaceByRow(row: anorm.Row) = {
    Race(row[Int]("id"), row[String]("name"), row[Int]("dex_mod"),
      row[Int]("str_mod"), row[Int]("cha_mod"), row[Int]("tou_mod"), row[Int]("wil_mod"),
      row[Int]("per_mod"), row[Int]("k_step"), row[Int]("kar_start"),
      row[Int]("kar_max"), row[Int]("kar_cost"), row[Int]("movement"),
      row[String]("abilities"), row[Int]("social_def"),
      row[Int]("spell_def"), row[Int]("rec_test"), row[Int]("phys_arm"),
      row[Int]("wound_tresh"))
  }
  
  /**
   * Get a Race By id
   * 
   * @param id 
   * @return Discipline
   */ 
  def getRaceById(id: Int): Race = {
    DB.withConnection("chars") { implicit c =>
      val querry = SQL(eoceneSqlStrings.GET_RACE_BY_ID).onParams(id)()
      getRaceByRow(querry.head)
    }
  }

  implicit object RaceFormat extends Format[Race] {

    def reads(json: JsValue) = JsSuccess(getRaceById(1))

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
