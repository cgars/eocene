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
import java.sql.Connection

case class Power(val id_armor:Int, val threads:Int, effect:String, value:Int)

object Power{
  
  /**
   * Get a Power created from row
   * 
   * @param rows the rows from a db call 
   * @return Discipline
   */ 
  def getPowerByRow(row: anorm.Row):Power = {
    Power(row[Int]("Powers.id_armor"), row[Int]("thread"), row[String]("effect"), 
        row[Int]("value"))
  }
  
  /**
   * Get power that belong to a Armor
   * 
   * @param id The id of the armmor
   * @return Discipline
   */ 
  def getPowersByArmorId(id:Int)(implicit c:Connection ) = {
    val querry = eoceneSqlStrings.GET_POWR_BY_ARMOR_ID.onParams(id)()
    querry.map(row=>getPowerByRow(row)).toList
  }
  
  implicit object RaceFormat extends Format[Power] {

    def reads(json: JsValue) = JsSuccess(Power(0,0,"",0))

    def writes(power: Power) = JsObject(
        Seq("id_armor" -> JsNumber(power.id_armor),
            "threads" -> JsNumber(power.threads),
            "value" -> JsNumber(power.value),
            "id_armor" -> JsString(power.effect)))
  }

}
