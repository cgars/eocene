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



/**
* A Character
*/  
case class Char(val id: Int, val name: String, val dex_mod: Int, val str_mod: Int,
  val cha_mod: Int, val tou_mod: Int, val wil_mod: Int, val per_mod: Int, val dex_level: Int,
  val str_level: Int, val cha_level: Int, val tou_level: Int,
  val will_level: Int, val per_level: Int, val lp_av: Int, val lp_sp: Int,
  val kar_curr: Int, val pp: Int, val race: Race, val disciplines: List[Discipline],
  val talents: List[Talent], val spells: List[Spell], val skills: List[Skill],
  val armors:List[Armor],val derived: Map[String, Any]) {
   
  /**
   * Get the Dice for this Talent
   *
   * @param step
   * @return A string indicating the dices to be used
   */  
  def getDice(step:Int) = eoceneServices.utilities.getDiceForStep(step)
   
  /**
   * Get a validator object for this character
   *
   * @return A Validator for this character
   */  
  def getValidator:eoceneServices.Validator = {
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
  def getModifierValueByName(name:String) = {
    disciplines .size match {
      case 0 => None
      case _ => Some(disciplines.map(disci => disci.getModifierValueByName(name).getOrElse(0))
      .reduce((a1,a2)=>max(a1,a2)))
    }
  } 
}

  /**
   * This Object is not only an Char factory but also the central DAL Object
   * for eocene Chars
   */  
object Char {
  /**
   * Get the id of a character with a certain name
   *
   * @param name
   * @return The first chharacter with that name or None 
   */  
  def getCharIdByName(name: String) = {
    DB.withConnection("chars") { implicit c =>
      val querry = SQL(eoceneSqlStrings.GET_CHAR_ID_FROM_NAME).onParams(name)()
      querry.size match{
        case 0 => None
        case _ => Some(querry(0)[Int]("id"))
      }
    }
  }
  
  /**
   * Get the id of a character with a certain name
   *
   * @param id 
   * @return Creates the character of that id from the database
   */ 
  def getCharById(id: Int)(implicit c: Connection) = {
    val char_querry = eoceneSqlStrings.GET_CHAR_BY_ID.onParams(id)()
    char_querry.size match{
      case 0 => None
      case _ => 
	    val race_querry = eoceneSqlStrings.RACE_JOIN.onParams(id)()
	    //race is guarantied to exist!
	    val race = Race.getRaceByRow(race_querry.head)
	    val disciplines_querry = eoceneSqlStrings.DISCIPLINE_JOIN.onParams(id)().
	    groupBy(row=>row[Int]("Disciplines.id"))
	    val disciplines = disciplines_querry.map(rows => 
	      Discipline.getDisciplineByRow(rows._2 )).toList
	    val talent_querry = eoceneSqlStrings.TALENT_JOIN.onParams(id)()
	    val talents = utilities.popDurability(talent_querry.map(
	      row => Talent.getTalentByRow(row)).toList)
	    val spell_querry = eoceneSqlStrings.SPELL_JOIN.onParams(id)()
	    val spells = spell_querry.map(row => Spell.getSpellByRow(row)).toList
	    val skill_querry = eoceneSqlStrings.SKILL_JOIN.onParams(id)()
	    val skills = skill_querry.map(row => Skill.getSkillByRow(row)).toList
	    val armor_map = eoceneSqlStrings.GET_ARMOR.onParams(id)().
	    groupBy(row=>row[Int]("Armors.id"))
	    val armors = armor_map.map(rows=> Armor.getArmorByRows(rows._2)).toList 	    
	    val row = char_querry.head
	    val derived_values = getDerivedValues(row, race, talents,armors, 
	        disciplines)
	    Some(Char(row[Int]("id"), row[String]("name"), row[Int]("dex_mod"),
	      row[Int]("str_mod"), row[Int]("cha_mod"), row[Int]("tou_mod"), 
	      row[Int]("wil_mod"), row[Int]("per_mod"), row[Int]("dex_level"), 
	      row[Int]("str_level"), row[Int]("cha_level"), row[Int]("tou_level"), 
	      row[Int]("wil_level"), row[Int]("per_level"), row[Int]("lp_av"), 
	      row[Int]("lp_sp"), row[Int]("kar_curr"), row[Int]("pp"), race, 
	      disciplines, talents, spells, skills, armors, derived_values))
    }
  }
  
  /**
   * Calculate the Values that depend on the characteristic of a charractor.
   *
   * @param row A row representing the character
   * @param race the characters Race
   * @param talents the characters talents
   * @param armors the characters armors 
   * @return a map of values
   * 
   *  The methods should be moved to the class and only called after the Char
   *  object was created. 
   */
  def getDerivedValues(row: anorm.Row, race: Race, talents: List[Talent],
      armors:List[Armor], disciplines:List[Discipline]): Map[String, Any] = {   
    val attributes = Map(
      "dex" -> (5 + row[Int]("dex_mod") + row[Int]("dex_level") + race.dex_mod),
      "str" -> (5 + row[Int]("str_mod") + row[Int]("str_level") + race.str_mod),
      "cha" -> (5 + row[Int]("cha_mod") + row[Int]("cha_level") + race.cha_mod),
      "tou" -> (5 + row[Int]("tou_mod") + row[Int]("tou_level") + race.tou_mod),
      "wil" -> (5 + row[Int]("wil_mod") + row[Int]("wil_level") + race.wil_mod),
      "per" -> (5 + row[Int]("per_mod") + row[Int]("per_level") + race.per_mod))
    val result = Map(
      "dex" -> (5 + row[Int]("dex_mod") + row[Int]("dex_level") + race.dex_mod),
      "str" -> (5 + row[Int]("str_mod") + row[Int]("str_level") + race.str_mod),
      "cha" -> (5 + row[Int]("cha_mod") + row[Int]("cha_level") + race.cha_mod),
      "tou" -> (5 + row[Int]("tou_mod") + row[Int]("tou_level") + race.tou_mod),
      "wil" -> (5 + row[Int]("wil_mod") + row[Int]("wil_level") + race.wil_mod),
      "per" -> (5 + row[Int]("per_mod") + row[Int]("per_level") + race.per_mod),
      "dex_step" -> eoceneServices.utilities.getAttrStep(attributes("dex")),
      "str_step" -> eoceneServices.utilities.getAttrStep(attributes("str")),
      "cha_step" -> eoceneServices.utilities.getAttrStep(attributes("cha")),
      "tou_step" -> eoceneServices.utilities.getAttrStep(attributes("tou")),
      "wil_step" -> eoceneServices.utilities.getAttrStep(attributes("wil")),
      "per_step" -> eoceneServices.utilities.getAttrStep(attributes("per")),
      "physDef" -> (eoceneServices.utilities.getAttrDefense(attributes("dex")) +
    		  		(disciplines.size match{
    		  		case 0 => 0 
    		  		case _ => disciplines.map(discipline=>
      				  discipline.getModifierValueByName("physDef").getOrElse(0))
      				  .reduce((a1,a2)=>a1+a2)
      				  })),
      "spellDef" -> (eoceneServices.utilities.getAttrDefense(attributes("per")) +
    		  		race.spell_def + 
    		  		(disciplines.size match{
    		  		case 0 => 0 
    		  		case _ => 
    		  		  disciplines.map(discipline=>
      				  discipline.getModifierValueByName("spellDef").getOrElse(0))
      				  .reduce((a1,a2)=>a1+a2)
      				  })),
      "socDef" -> (eoceneServices.utilities.getAttrDefense(attributes("cha")) +
        		  race.social_def + 
    		  		(disciplines.size match{
    		  		case 0 =>0 
    		  		case _ => 
    		  		  disciplines.map(discipline=>
      				  discipline.getModifierValueByName("socDef").getOrElse(0))
      				  .reduce((a1,a2)=>a1+a2)
      				  })),
      "movement" -> eoceneServices.utilities.getAttrMovement(attributes("dex") +
        race.movement),
      "carrying" -> eoceneServices.utilities.getAttrCarrying(attributes("str")),
      "death" -> (eoceneServices.utilities.getAttrDeath(attributes("tou")) +
        utilities.getDurability(talents)(0)),
      "unconc" -> (eoceneServices.utilities.getAttrUnconc(attributes("tou")) +
        utilities.getDurability(talents)(1)),
      "wound" -> eoceneServices.utilities.getAttrWound(attributes("tou")),
      "rec" -> (eoceneServices.utilities.getAttrRec(attributes("tou"))+
    		  	race.rec_test + 
    		  	(disciplines.size match{
    		  		case 0 =>0 
    		  		case _ => 
    		  		  disciplines.map(discipline=>
      				  discipline.getModifierValueByName("rec").getOrElse(0))
      				  .reduce((a1,a2)=>a1+a2)
      				  })),
      "mystic" -> (armors.size match{//make sure that we wont call reduce on emtpy list
        	case 0 => eoceneServices.utilities.getAttrMystic(attributes("wil"))
      		case _ => eoceneServices.utilities.getAttrMystic(attributes("wil")) + 
        		   armors.map(armor=>armor.getMysticalArmor()).
        		   reduce((a1,a2)=>a1+a2)}),
      "physical" -> (armors.size match{//make sure that we wont call reduce on empty list 
        	case 0 => 0
        	case _ => armors.map(armor=>armor.getPhysicalArmor()).
        			  reduce((a1,a2)=>a1+a2)
         }),
      "initiative" -> ((armors.size match{//make sure that we wont call reduce on emtpy list
        	case 0 => eoceneServices.utilities.getAttrStep(attributes("dex"))
      		case _ => eoceneServices.utilities.getAttrStep(attributes("dex")) + 
        		   armors.map(armor=>armor.getInitiativeBonus()).
        		   reduce((a1,a2)=>a1+a2)}) + 
        	(disciplines.size match{
    		  		case 0 =>0 
    		  		case _ => 
    		  		  disciplines.map(discipline=>
      				  discipline.getModifierValueByName("ini").getOrElse(0))
      				  .reduce((a1,a2)=>a1+a2)
      				  }))     	
      )

    return result
  }
  
  /**
   * Create a character with a name. ensure race
   *
   * @param name 
   * @param user the User object that models the apllication user. Comes from
   * 			 the controller.   
   * @return the id of the new character
   */
  def createCharByName(name: String)(implicit user:eoceneServices.EoceneUser) = {
    DB.withTransaction("chars") { implicit c =>      
      val result: Boolean = SQL(eoceneSqlStrings.INSERT_CHAR).onParams(name).executeUpdate()>0
      val char_id = getCharIdByName(name)
      changeCharRace(char_id.get, 1)
      eoceneSqlStrings.INSERT_CHARS_USERS .onParams(char_id, 
          user.main.userId ).execute()
       char_id
    }    
  }

  def getCharAttribute(id: Int, attribute: String)(implicit c: Connection):Option[Int] =  {
    val querry = SQL(eoceneSqlStrings.GET_CHAR_ATTRIBUTE).onParams(id)()
    querry.size match {
      case 0 => None
      case _ => querry(0)[Option[Int]](attribute)
    }
  }
  
    /**
   * Update an attrribute
   *
   * @param id the id of the character
   * @param attribute the name of the atrribute (eg. "dex_mod")
   * @param direction either "up" or "down"
   * @param c A sql connection. comes from the controller
   * @return Success indicator
   */
  def updateCharAttribute(id: Int, attribute: String, new_value: Int)(
      implicit c: Connection): Boolean= {
    val result = SQL(eoceneSqlStrings.UPDATE_CHAR_ATTRIBUTE.format(attribute)).
      onParams(new_value, id).executeUpdate()
     Logger.info("attr:%s,new:%s, result:%s".format(attribute,new_value, result))
    return result>0
  }
  
  /**
   * Update an attrribute using Purchase points
   *
   * @param id the id of the character
   * @param attribute the name of the atrribute (eg. "dex_mod")
   * @param direction either "up" or "down"
   * @param direction either "up" or "down"
   * @param c A sql connection. comes from the controller
   * @return Succes indicator
   */
  def updateCharAttributeWithPP(id: Int, attribute: String, direction: String): Boolean = {
    DB.withConnection("chars") { implicit c =>
      val current_value = getCharAttribute(id, attribute)
      Logger.info("current_value:%s".format(current_value))
      val pp = getCharAttribute(id, "pp")
      if (direction.equals("up")) {
        current_value match{
          case None => return false //if this is not None then the char and the atrribute exist!
          case _ => current_value.get match {
            case 13 => return false
            case _ =>
	        updateCharAttribute(id, attribute, current_value.get + 1)
	        updateCharAttribute(id, "pp", pp.get +
	          eoceneServices.utilities.getPPCost(current_value.get + 1) -
	          eoceneServices.utilities.getPPCost(current_value.get))
          }
  
        }
      } else {
          current_value match{
          case None => return false 
          case _ => current_value.get match {
            case -3 => return false
            case _ =>
	        updateCharAttribute(id, attribute, current_value.get - 1)
	        updateCharAttribute(id, "pp", pp.get +
	          eoceneServices.utilities.getPPCost(current_value.get - 1) -
	          eoceneServices.utilities.getPPCost(current_value.get))
          }
         }
      }
    }
  }

   /**
   * Update an attrribute using Legend points
   *
   * @param id the id of the character
   * @param attribute the name of the atrribute (eg. "dex_mod")
   * @param direction either "up" or "down"
   * @param c a sql connection. comes from the controller
   * @return Succes indicator
   */
  def updateCharAttributeWithLP(id: Int, attribute: String, direction: String): Boolean = {
    DB.withTransaction("chars") { implicit c =>
      val current_value = getCharAttribute(id, attribute)
      val lp = getCharAttribute(id, "lp_sp")
      if (direction.equals("up")) {
          current_value match{
          case None => return false
          case _ => current_value.get match {
            case 5 => return false
            case _ =>
	        updateCharAttribute(id, attribute, current_value.get + 1)
	        updateCharAttribute(id, "lp_sp", lp.get +
	          eoceneServices.utilities.getAttributeIncreaseLPCost(current_value.get + 1))
          	}
          }
      } else {
          current_value match{
          case None => return false
          case _ => current_value.get match {
            case 0 => return false
            case _ =>
		        updateCharAttribute(id, attribute, current_value.get - 1)
		        updateCharAttribute(id, "lp_sp", lp.get -
		          eoceneServices.utilities.getAttributeIncreaseLPCost(current_value.get))
          	}
          }
      }
    }
  }

   /**
   * Get the Race of a character
   *
   * @param id the id of the character
   * @param c a sql connection. comes from the controller
   * @return Succes indicator
   */
  def getRaceIdByCharId(id: Int)= {
    DB.withConnection("chars") { implicit c =>
      val querry = SQL(eoceneSqlStrings.GET_CHAR_RACE).onParams(id)()
      querry.length match {
        case 0 => None 
        case _ => Some(querry(0)[Int]("id_race"))
      }
     }
  }
   
  /**
   * Change the race of an charcter
   *
   * @param id the id of the character
   * @param id_race 
   * @param c a sql connection. comes from the controller
   * @return Succes indicator
   */
  def changeCharRace(id: Int, id_race: Int): Boolean = {
    DB.withConnection("chars") { implicit c =>
      getRaceIdByCharId(id) match {
        case None => SQL(eoceneSqlStrings.SET_CHAR_RACE).
          onParams(id, id_race).executeUpdate()>0
        case _ =>           SQL(eoceneSqlStrings.UPATE_CHAR_RACE).
          onParams(id_race, id).executeUpdate()>0
      	}
      }
    }
  
  /**
   * Get  A List representing the chars_disciplines table entries for this 
   * character
   *
   * @param id the id of the character
   * @return A nestetd List of integers corresponding to id_char,id_discipline 
   * 		 and circle
   */
  def getCharDisciplineRowsByCharId(id: Int): List[List[Int]] = {
    DB.withConnection("chars") { implicit c =>
      {
        val querry = SQL(eoceneSqlStrings.GET_CHAR_DISCIPLINES).onParams(id)()
        return querry.map(a => List(a[Int]("id_char"), a[Int]("id_discipline"),
          a[Int]("circle"))).toList
      }
    }
  }
  
  /**
   * Improve the discipline of a charcter. in case the character does not yet 
   * have the discipline, add it.
   *
   * @param id the id of the character
   * @param id_discipline
   * @return success
   */
  def improveCharDiscipline(id: Int, id_discipline: Int) = {
    DB.withConnection("chars") { implicit c =>
      {
        val disciplines = getCharDisciplineRowsByCharId(id)
        val target_discipline = disciplines.filter(a => a(1) == id_discipline)
            if (target_discipline.length > 0) {
	          val circle = target_discipline(0)(2)
	          SQL(eoceneSqlStrings.UPDATE_CHAR_DISCIPLINE).onParams(circle + 1, id,
	            id_discipline).executeUpdate()>0
	        } else {
	          SQL(eoceneSqlStrings.INSERT_CHAR_DISCIPLINE).onParams(id, id_discipline, 1)
            .executeUpdate()>0
        	}        
      }
    }
  }
  
  /**
   * Corrrupt the discipline of a charcter. In case the character we ar in 
   * circle 1 -> remove the discipline 
   *
   * @param id the id of the character
   * @param id_discipline
   * @return success
   */
  def corruptCharDiscipline(id: Int, id_discipline: Int) = {
    DB.withConnection("chars") { implicit c =>
      {
        val disciplines = getCharDisciplineRowsByCharId(id)
        val target_discipline = disciplines.filter(a => a(1) == id_discipline)
        target_discipline.size match {
          case 0 => false
          case _ => 
            val circle = target_discipline(0)(2)        
	        if (circle > 1) {
	          SQL(eoceneSqlStrings.UPDATE_CHAR_DISCIPLINE).onParams(circle - 1, id,
	            id_discipline).executeUpdate()>0
	        } else {
	          SQL(eoceneSqlStrings.REMOVE_CHAR_DISCIPLINE).onParams(id, id_discipline)
	          .executeUpdate()>0
	        	}
        }
      }
    }
  }

    /**
   * Get data for talents of a charcter
   *
   * @param id the id of the character
   * @param id_discipline
   * @return a nested list of integers with id_char, id_talent and step 
   * 		 respectively
   */
  def getCharTalentRowsIdByCharId(id: Int): List[List[Int]] = {
    DB.withConnection("chars") { implicit c =>
      {
        val querry = SQL(eoceneSqlStrings.GET_CHAR_TALENTS).onParams(id)()
        return querry.map(a => List(a[Int]("id_char"), a[Int]("id_talent"),
          a[Int]("step"))).toList
      }
    }
  }
  
    /**
   * Get data for talents of a character while ensuring that the circle is the 
   * lowest
   *
   * @param id the id of the character
   * @param id_discipline
   * @return success
   */
  def getTalentByTalenAndDisciId(id_talent: Int, id_char: Int)(implicit c: Connection) = {
    val querry = SQL(eoceneSqlStrings.GET_TALENT_CIRCLE).onParams(
      id_char, id_talent)()
    querry(0)[Int]("circle")
  }
   
   /**
   * Improve a talent (or learn it)
   *
   * @param id the id of the character
   * @param id_talent
   * @return success
   */
  def improveCharTalent(id: Int, id_talent: Int):Boolean = {
    DB.withConnection("chars") { implicit c =>
      {
        val talents = getCharTalentRowsIdByCharId(id)
        val target_talent = talents.filter(a => a(1) == id_talent)
        val circle = getTalentByTalenAndDisciId(id_talent, id)

        if (target_talent.length > 0) {
          val step = target_talent(0)(2)
          if (step==15) return false
          val lp_cost = eoceneServices.utilities.getTalentCost(step + 1, circle)
          SQL(eoceneSqlStrings.UPDATE_CHAR_TALENT).onParams(step + 1, id,
            id_talent).executeUpdate()>0
          SQL(eoceneSqlStrings.ADD_TO_LP).onParams(lp_cost, id).executeUpdate()>0
        } else {
          val lp_cost = eoceneServices.utilities.getTalentCost(1, circle)
          SQL(eoceneSqlStrings.INSERT_CHAR_TALENT).onParams(id, id_talent, 1)
            .executeUpdate()>0
          SQL(eoceneSqlStrings.ADD_TO_LP).onParams(lp_cost, id)
          	.executeUpdate()>0
        }
      }
    }
  }
   
   /**
   * Remove one circle (or forget)
   *
   * @param id the id of the character
   * @param id_talent
   * @return success
   */
  def corruptCharTalent(id: Int, id_talent: Int):Boolean = {
    DB.withConnection("chars") { implicit c =>
      {
        val talents = getCharTalentRowsIdByCharId(id)
        val target_talent = talents.filter(a => a(1) == id_talent)
        if (target_talent.size==0) return false
        val circle = getTalentByTalenAndDisciId(id_talent, id)
        val step = target_talent(0)(2)
        val lp_cost = eoceneServices.utilities.getTalentCost(step, circle)
        if (step > 1) {
          SQL(eoceneSqlStrings.UPDATE_CHAR_TALENT).onParams(step - 1, id,
            id_talent).executeUpdate()>0
          SQL(eoceneSqlStrings.ADD_TO_LP).onParams(-lp_cost, id)
          	.executeUpdate()>0
        } else {
          SQL(eoceneSqlStrings.REMOVE_CHAR_TALENT).onParams(id, id_talent)
           .executeUpdate()>0
          SQL(eoceneSqlStrings.ADD_TO_LP).onParams(-lp_cost, id)
          .executeUpdate()>0
        }
      }
    }
  }

  
  def getCharSkillRowsByCharId(id: Int): List[List[Int]] = {
    DB.withConnection("chars") { implicit c =>
      {
        val querry = SQL(eoceneSqlStrings.GET_CHAR_SKILLS).onParams(id)()
        return querry.map(a => List(a[Int]("id_char"), a[Int]("id_skill"),
          a[Int]("step"))).toList
      }
    }
  }

   /**
   * Improne a skill (or learn it)
   *
   * @param id the id of the character
   * @param id_skill
   * @return success
   */
  def improveCharSkill(id: Int, id_skill: Int): Boolean = {
    DB.withTransaction("chars") { implicit c =>
      {
        val skills = getCharSkillRowsByCharId(id)
        val target_skill = skills.filter(a => a(1) == id_skill)
        if (target_skill.length > 0) {
          val step = target_skill(0)(2)
          if (step==15) return false
          SQL(eoceneSqlStrings.UPDATE_CHAR_SKILL).onParams(step + 1, id,
            id_skill).executeUpdate()>0
          SQL(eoceneSqlStrings.ADD_TO_LP).onParams(
            utilities.getSkillLPCcost(step + 1), id).executeUpdate()>0
        } else {
          SQL(eoceneSqlStrings.INSERT_CHAR_SKILL).onParams(id, id_skill, 1)
            .executeUpdate()>0
          SQL(eoceneSqlStrings.ADD_TO_LP).onParams(
            utilities.getSkillLPCcost(1), id).executeUpdate()>0
        }
      }
    }
  }
   /**
   * Corrupt a skill (or forget it)
   *
   * @param id the id of the character
   * @param id_skill
   * @return success
   */
  def corruptCharSkill(id: Int, id_skill: Int):Boolean = {
    DB.withTransaction("chars") { implicit c =>
      {
        val skills = getCharSkillRowsByCharId(id)
        val target_skill = skills.filter(a => a(1) == id_skill)
        if (target_skill.size==0) return false
        val step = target_skill(0)(2)
        if (step > 1) {
          SQL(eoceneSqlStrings.UPDATE_CHAR_SKILL).onParams(step - 1, id,
            id_skill).executeUpdate()>0
          SQL(eoceneSqlStrings.ADD_TO_LP).onParams(
            -utilities.getSkillLPCcost(step), id).executeUpdate()>0
        } else {
          SQL(eoceneSqlStrings.REMOVE_CHAR_SKILL).onParams(id, id_skill)
            .executeUpdate()>0
          SQL(eoceneSqlStrings.ADD_TO_LP).onParams(
            -utilities.getSkillLPCcost(1), id).executeUpdate()>0
        }
      }
    }
  }
   
  /**
   * Return all Spells of a character
   *
   * @param id the id of the character
   * @return A nested list
   */
  def getCharSpellListByCharId(id: Int): List[List[Int]] = {
    DB.withConnection("chars") { implicit c =>
      {
        val querry = SQL(eoceneSqlStrings.GET_CHAR_SPELLS).onParams(id)()
        return querry.map(a => List(a[Int]("id_char"), a[Int]("id_spell"),
          a[Int]("step"))).toList
      }
    }
  }
  /**
   * Learn a spell
   *
   * @param id id of the character
   * @param id_spell  
   * @return success
   */
  def learnCharSpell(id: Int, id_Spell: Int) = {
    DB.withConnection("chars") { implicit c =>
      {
        SQL(eoceneSqlStrings.INSERT_CHAR_SPELL).onParams(id,
          id_Spell,None).executeUpdate()>0
      }
    }
  }
  
  /**
   * Forget a spell
   *
   * @param id id of the character
   * @param id_spell  
   * @return success
   */
  def unlearnCharSpell(id: Int, id_Spell: Int) = {
    DB.withConnection("chars") { implicit c =>
      {
        SQL(eoceneSqlStrings.REMOVE_CHAR_SPELL).onParams(id, id_Spell,None)
        .executeUpdate()>0
      }
    }
  }

 
  /**
   * Change the anme of the character
   *
   * @param id id of the character
   * @param name teh new name
   * @return success
   */
  def changeCharName(id: Int, name: String) = {
    DB.withConnection("chars") { implicit c =>
      {
        SQL(eoceneSqlStrings.UPATE_CHAR_NAME).onParams(name, id)
        .executeUpdate()>0
      }
    }
  }
 
  /**
   * Add armor
   *
   * @param id id of the character
   * @param id_armor
   * @return success
   */
  def getArmor(id:Int,id_armor:Int) ={
     DB.withConnection("chars") { implicit c =>
       eoceneSqlStrings.INSERT_ARMOR.onParams(id, id_armor).executeUpdate()>0
     }    
  }
  
  /**
   * Remove
   *
   * @param id id of the character
   * @param id_armor
   * @return success
   */
  def removeArmor(id:Int,id_armor:Int) ={
     DB.withConnection("chars") { implicit c =>
       eoceneSqlStrings.REMOVE_ARMOR.onParams(id, id_armor).executeUpdate()>0
     }    
  }
  
  /**
   * Attach a threat to an armor
   *
   * @param id id of the character
   * @param id_armor
   * @return success
   */
  def attachThreadArmor(id:Int,id_armor:Int) ={
     DB.withConnection("chars") { implicit c =>
       eoceneSqlStrings.UPATE_ARMOR .onParams(1,id, id_armor).executeUpdate()>0
     }    
  }
  
  /**
   * Remove a threat from an armor
   *
   * @param id id of the character
   * @param id_armor
   * @return success
   */
  def removeThreadArmor(id:Int,id_armor:Int) ={
     DB.withConnection("chars") { implicit c =>
       eoceneSqlStrings.UPATE_ARMOR .onParams(-1,id, id_armor).executeUpdate()>0
     }    
  }
  
  /**
   * Add a spell to a matrix
   *
   * @param id_spell id of the spell
   * @param id_char id of the character
   * @return success
   */
  def Spell2Matrix(id_spell:Int,id_char:Int) ={
     DB.withConnection("chars") { implicit c =>
       eoceneSqlStrings.ADD_SPELL_2_MATRIX.onParams(id_spell, id_char)
       .executeUpdate()>0
     }    
  }

  /**
   * Remove a spell from a matrix
   *
   * @param id_spell id of the spell
   * @param id_char id of the character
   * @return success
   */
  def SpellFromMatrix(id_spell:Int,id_char:Int) = {
     DB.withConnection("chars") { implicit c =>
       eoceneSqlStrings.REMOVE_SPELL_FROM_MATRIX.onParams(id_spell, id_char)
       .executeUpdate()>0
     }    
  }
  
  def removeUserFromChar(id_char:Int, id_user:String) = {
    DB.withConnection("chars") { implicit c =>
      eoceneSqlStrings.REMOVE_USER_FROM_CHAR .onParams(id_char, id_user)
      .executeUpdate>0
    }
    
  }

  implicit object CharonParams extends Format[Char] {

    def reads(json: JsValue) = DB.withConnection("chars") {
      implicit c => JsSuccess(Char.getCharById(1).get)
    }

    def writes(char: Char) = JsObject(Seq(
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
// def getTalentRowsByCharId(id: Int): List[Talent] = {
//    DB.withConnection("chars") { implicit c =>
//      {
//        val querry = eoceneSqlStrings.TALENT_JOIN.onParams(id)()
//        querry.map(row => parseTalentRow(row)).toList
//
//      }
//    }
//  }
//
//  def parseTalentRow(row: anorm.Row) = {
//    if (row[Option[Int]]("chars_talents.id_talent") == None) {
//      Talent.getTalent(row[Int]("Talents.id"), row[String]("Talents.name"),
//        row[Boolean]("Talents.action"), row[Boolean]("Talents.karma"),
//        row[String]("Talents.strain"), row[String]("Talents.formula"), 0, row[Boolean]("talents_disciplines.disciplined"),
//        row[Int]("talents_disciplines.circle"),
//        row[Int]("talents_disciplines.discipline_id"))
//    } else {
//      Talent.getTalent(row[Int]("Talents.id"), row[String]("Talents.name"),
//        row[Boolean]("Talents.action"), row[Boolean]("Talents.karma"),
//        row[String]("Talents.strain"), row[String]("Talents.formula"), row[Int]("chars_talents.step"), row[Boolean]("talents_disciplines.disciplined"),
//        row[Int]("talents_disciplines.circle"),row[Int]("talents_disciplines.discipline_id"))
//    }
//  }
//
//  def getSpellRowsByCharId(id: Int) = {
//    DB.withConnection("chars") { implicit c =>
//      {
//        val querry = eoceneSqlStrings.SPELL_JOIN.onParams(id)()
//        querry.map(row => Spell.getSpell(row[Int]("id"), row[String]("name"),
//          row[Int]("Spells.circle"), row[Int]("threads"), row[Int]("weaving_difficulty"), row[Int]("reatuning"), row[String]("runge"), row[String]("duration"),
//          row[String]("effect"), row[String]("difficulty"),
//          row[String]("description"), row[Option[Int]]("chars_spells.id_char"),
//          row[Option[Int]]("chars_spells.id_discipline"),
//          row[Option[Int]]("chars_spells.spell_matrix"))).toList
//      }
//    }
//  }
//
//  def getSkillRowsByCharId(id: Int) = {
//    DB.withConnection("chars") { implicit c =>
//      {
//        val querry = eoceneSqlStrings.SKILL_JOIN.onParams(id)()
//        querry.map(row => Skill.getSkill(row[Int]("id"), row[String]("name"), row[String]("formula"),
//          row[String]("type"), row[String]("comm"), row[Option[Int]]("step"))).toList
//      }
//    }
//  }
}
