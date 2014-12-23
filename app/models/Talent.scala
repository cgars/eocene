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
import eoceneServices.utilities

case class Talent(val id: Int, val name: String, val action: Boolean,
  val karma: Boolean, val strain: String, val formula: String, val step: Option[Int],
  val disciplined: Option[Boolean], val circle: Option[Int],
  val discipline_id: Option[Int]){
  
    /**
   * Returns the Rank the talent currently has
   * 
   * This methods checks for the attributed as defined in the talent formula
   * and if neccesary (according to the formula) it adds a modifier.      
   *
   * @param char The Character who has this talent
   * @return The rank of this talent or 0
   */  
  def getRank(char:Char) = {
    val formula_split = formula.split("\\+")// Split the formula (for cases like Dex+3)
    step match{
	      case None => 0 //in case we have no step rank is not defined
	      case _ => //First we do the formula attribute conversion
	      val rank = formula_split(0) match{
		    case "Dex" => char.derived("dex_step").asInstanceOf[Int]+step.get
		    case "Str" => char.derived("str_step").asInstanceOf[Int]+step.get
		    case "Tou" => char.derived("tou_step").asInstanceOf[Int]+step.get
		    case "Cha" => char.derived("cha_step").asInstanceOf[Int]+step.get
		    case "Per" => char.derived("dex_step").asInstanceOf[Int]+step.get
		    case "Wil" => char.derived("wil_step").asInstanceOf[Int]+step.get
		    case _ => 0
		  }
	      //return the derived rank or add the second part of formula
	      formula_split.size match{ 
	      case 1 => rank 
	      case _ => rank + formula_split(1).toInt
	    }	    
    }
  }
  
   /**
   * Return the Dice for this Talent
   *
   * @param char The Character who has this talent
   * @return The Dice String for this talent
   */  
  def getDice(char:Char) = eoceneServices.utilities.getDiceForStep(getRank(char))

}

object Talent {
    
  /**
   *	Returns a Talent created from the entries in the row  
   *
   * @param row: A row from a db querry
   * @return Talent 
   */ 
  def getTalentByRow(row: anorm.Row) = {
    Talent(row[Int]("id"), row[String]("name"), row[Boolean]("action"),
      row[Boolean]("karma"), row[String]("strain"), row[String]("formula"),
      row[Option[Int]]("step"), row[Option[Boolean]]("disciplined"),
      row[Option[Int]]("talents_disciplines.circle"),
      row[Option[Int]]("talents_disciplines.id_discipline"))
  }
  
  /**
   * Return the Talent with id  
   *
   * @param row: A row from a db querry
   * @return Talent 
   */ 
  def getTalentById(id: Int, step: Int = 0, circle: Int = 1, 
      disciplined: Boolean = false)(
      implicit c: Connection): Talent = {
    val querry = eoceneSqlStrings.TALENT_JOIN.onParams(id)()
    getTalentByRow(querry.head)
  }

  def getTalent(id: Int, name: String, action: Boolean, karma: Boolean, strain: String,
    formula: String, step: Int, disciplined: Boolean, circle: Int,discipline_id:Int) = {
    Talent(id: Int, name: String, action: Boolean, karma: Boolean, strain: String,
      formula: String, Option(step), Option(disciplined), Some(circle),Option(discipline_id))
  }

  implicit object TalentFormat extends Format[Talent] {

    def reads(json: JsValue) = {
      DB.withConnection("chars") { implicit c =>
        JsSuccess(getTalentById(1))
      }
    }

    def writes(talent: Talent) = talent.step match {

      case None => JsObject(Seq("id" -> JsNumber(talent.id),
        "name" -> JsString(talent.name),
        "action" -> JsBoolean(talent.action),
        "karma" -> JsBoolean(talent.karma),
        "strain" -> JsString(talent.strain),
        "formula" -> JsString(talent.formula),
        "step" -> JsNumber(0),
        "disciplined" -> JsBoolean(false),
        "circle" -> JsNumber(0)))

      case _ => JsObject(Seq("id" -> JsNumber(talent.id),
        "name" -> JsString(talent.name),
        "action" -> JsBoolean(talent.action),
        "karma" -> JsBoolean(talent.karma),
        "strain" -> JsString(talent.strain),
        "formula" -> JsString(talent.formula),
        "step" -> JsNumber(talent.step.get),
        "disciplined" -> JsBoolean(talent.disciplined.get),
        "circle" -> JsNumber(talent.circle.get)))

    }
  }
}
