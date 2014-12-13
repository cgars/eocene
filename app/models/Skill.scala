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

case class Skill(val id: Int, val name: String, val formula: String,
  val skill_type: String, val comm: String, step: Option[Int]){
    
  /**
   * Returns the Rank the skill currently has
   * 
   * This methods checks for the attributed as defined in the skill formula
   * and if neccesary (according to the formula) it adds a modifier.      
   *
   * @param char The Character who has this skill
   * @return The rank of this skill or 0
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
   * Return the Dice for this Skill
   *
   * @param char The Character who has this skill
   * @return The Dice String for this skill
   */  
  def getDice(char:Char) = eoceneServices.utilities.getDiceForStep(getRank(char))
  
  
}

object Skill {
  
  /**
   * Get a Skill build from row
   * 
   * @param rows the rows from a db call 
   * @return Discipline
   */ 
  def getSkillByRow(row: anorm.Row) = {
    Skill(row[Int]("id"), row[String]("name"), row[String]("formula"),
      row[String]("type"), row[String]("comm"), row[Option[Int]]("step"))
  }
  
  /**
   * Get a Skill by its id
   * 
   * @param id
   * @return Discipline
   */ 
  def getSkillById(id: Int): Skill = {
    DB.withConnection("chars") { implicit c =>
      val querry = SQL(eoceneSqlStrings.GET_SKILL_BY_ID).onParams(id)()
      getSkillByRow(querry(0))
    }
  }

  def getSkill(id: Int, name: String, formula: String, skill_type: String,
    comm: String, step: Option[Int]) = Skill(id, name, formula, skill_type, comm, step)

  implicit object TalentFormat extends Format[Skill] {

    def reads(json: JsValue) = JsSuccess(getSkillById(1))

    def writes(skill: Skill) = skill.step match {

      case None => JsObject(Seq("id" -> JsNumber(skill.id),
        "name" -> JsString(skill.name),
        "formula" -> JsString(skill.formula),
        "skill_type" -> JsString(skill.skill_type),
        "comm" -> JsString(skill.comm),
        "step" -> JsNumber(0)))

      case skill.step => JsObject(Seq("id" -> JsNumber(skill.id),
        "name" -> JsString(skill.name),
        "formula" -> JsString(skill.formula),
        "skill_type" -> JsString(skill.skill_type),
        "comm" -> JsString(skill.comm),
        "step" -> JsNumber(skill.step.get)))

    }
  }
}
