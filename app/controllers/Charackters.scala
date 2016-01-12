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
package controllers

import play.api._
import play.api.mvc._
import play.api.db._
import play.api.Play.current
import anorm._
import play.api.db.DB
import eoceneServices.eoceneSqlStrings
import eoceneServices.utilities
import play.api.libs.json.Json
import models.Character
import play.api.libs.json.JsString
import play.api.libs.json.JsNumber
import securesocial.core._
import eoceneServices.EoceneUser
import play.api.mvc.{ Action, RequestHeader }
import play.api.libs.json.JsNumber
import eoceneServices.eoceneUserService
import eoceneServices.eoceneSqlService
import eoceneServices.eoceneDao

/**
 * Main controller for modifications on characters
 */
class Charackters(override implicit val env: RuntimeEnvironment[EoceneUser],
    implicit val dao:eoceneServices.eoceneDao)
  extends securesocial.core.SecureSocial[EoceneUser] {
  /**
   * Get a character with id
   *
   * @param id
   * @return the character (JSON)
   */
  def getCharacterById(id: Int) = SecuredAction(UserAllowedWithCharacterId(id)) {
      val char = dao.getCharById(id)
      char match {
        case None => NotFound("")
        case Some(char) => Ok(Json.prettyPrint(Json.toJson(char)))
          .withHeaders(CACHE_CONTROL -> "no-cache",
            ETAG -> char.hashCode.toString)
            }
  }

  /**
   * Create a character with a name
   *
   * @param name
   * @return char id as Json
   */
  def create(name: String) = SecuredAction { implicit request =>
      implicit val user = request.user
      val char = dao.createCharByName(name)
      char match {
        case None => BadRequest("")
        case Some(char) => Created(Json.toJson(JsNumber(char)))
          .withHeaders(CACHE_CONTROL -> "no-cache",
            ETAG -> char.hashCode.toString)
    }
  }

  /**
   * Create a character with a random name
   *
   * @param name
   * @return char id as Json
   */
  def createWithoutName() = SecuredAction { implicit request =>
      implicit val user = request.user
      val result = dao.createCharByName(
        eoceneServices.utilities.getRandomName())
      result match {
        case None => BadRequest("")
        case Some(result) => Created(Json.toJson(JsNumber(result)))
          .withHeaders(CACHE_CONTROL -> "no-cache")
    }
  }

  /**
   * Improve an attribute consuming Legend points
   *
   * @param id character id
   * @param attribute the name of the attribute (eg. dex_level)
   * @return success
   */
  def improveAttributeLP(id: Int, attribute: String) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.updateCharAttributeWithLP(id, attribute, "up") match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              improveAttributeLP(id, attribute).toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * Reduce an attrribute gaining Legend Points
   *
   * @param id character id
   * @param attribute the name of the attribute (eg. dex_level)
   * @return success
   */
  def corruptAttributeLP(id: Int, attribute: String) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.updateCharAttributeWithLP(id, attribute, "down") match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.corruptAttributeLP(id,
              attribute).toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * Improve an attribute consuming Legend points
   *
   * @param id character id
   * @param attribute the name of the attribute (eg. dex_level)
   * @return success
   */
  def improveAttributePP(id: Int, attribute: String) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.updateCharAttributeWithPP(id, attribute, "up") match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              improveAttributePP(id, attribute).toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * Reduce an attrribute gaining Legend Points
   *
   * @param id character id
   * @param attribute the name of the attribute (eg. dex_level)
   * @return success
   */
  def corruptAttributePP(id: Int, attribute: String) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.updateCharAttributeWithPP(id, attribute, "down") match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              corruptAttributePP(id, attribute).toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * Change (or set) a characters Race
   *
   * @param id character id
   * @param id_race
   * @return success
   */
  def changeCharRace(id: Int, id_race: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.changeCharRace(id: Int, id_race: Int) match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              changeCharRace(id, id_race).toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * Advance the character in a given discipline
   *
   * @param id character id
   * @param id_race
   * @return success
   */
  def improveCharDiscipline(id: Int, id_discipline: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.improveCharDiscipline(id: Int, id_discipline: Int) match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              improveCharDiscipline(id: Int, id_discipline: Int)
              .toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * DisAdvance the character in a given discipline
   *
   * @param id character id
   * @param id_race
   * @return success
   */
  def corruptCharDiscipline(id: Int, id_discipline: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.corruptCharDiscipline(id: Int, id_discipline: Int) match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              corruptCharDiscipline(id: Int, id_discipline: Int)
              .toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * Improve the a talent
   *
   * @param id character id
   * @param id_talent
   * @return success
   */
  def improveCharTalent(id: Int, id_talent: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.improveCharTalent(id: Int, id_talent: Int) match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              improveCharTalent(id: Int, id_talent: Int).toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * Reduce a talent
   *
   * @param id character id
   * @param id_talent
   * @return success
   */
  def corruptCharTalent(id: Int, id_talent: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.corruptCharTalent(id: Int, id_talent: Int) match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              corruptCharTalent(id: Int, id_talent: Int).toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * Improve a skill (or add it)
   *
   * @param id character id
   * @param id_skill
   * @return success
   */
  def improveCharSkill(id: Int, id_skill: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.improveCharSkill(id: Int, id_skill: Int) match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              improveCharSkill(id: Int, id_skill: Int).toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * Reduce a skill (or remnove it)
   *
   * @param id character id
   * @param id_skill
   * @return success
   */
  def corruptCharSkill(id: Int, id_skill: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.corruptCharSkill(id: Int, id_skill: Int) match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              corruptCharSkill(id: Int, id_skill: Int).toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * Learn a spell
   *
   * @param id character id
   * @param id_spell
   * @return success
   */
  def learnCharSpell(id: Int, id_spell: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.learnCharSpell(id: Int, id_spell: Int) match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              learnCharSpell(id: Int, id_spell: Int).toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * Forget a spell
   *
   * @param id character id
   * @param id_spell
   * @return success
   */
  def unlearnCharSpell(id: Int, id_spell: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.unlearnCharSpell(id: Int, id_spell: Int) match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              unlearnCharSpell(id: Int, id_spell: Int).toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * Change the name of a character (name is read from the request)
   *
   * @param id character id
   * @return success
   */
  def changeCharName(id: Int) = SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
      val data = request.body.asJson
      val name = (data.get \ "name").asOpt[String]
      dao.changeCharName(id, name.get) match {
        case false => BadRequest("")
        case _ => Ok("")
    }
  }

  /**
   * Add armor to the character
   *
   * @param id character id
   * @param id_armor
   * @return success
   */
  def getArmor(id: Int, id_armor: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.getArmor(id, id_armor) match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              getArmor(id: Int, id_armor: Int).toString,
              id, request.user.main.userId)
            Ok("")
      }
    }
  
  /**
   * Remove armor from the character
   *
   * @param id character id
   * @param id_armor
   * @return success
   */
  def removeArmor(id: Int, id_armor: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.removeArmor(id, id_armor) match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              removeArmor(id: Int, id_armor: Int).toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * Attach a thread to an armor item
   *
   * @param id character id
   * @param id_armor
   * @return success
   */
  def attachThreadArmor(id: Int, id_armor: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.attachThreadArmor(id, id_armor) match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              attachThreadArmor(id: Int, id_armor: Int).toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * Remove a thread to an armor item
   *
   * @param id character id
   * @param id_armor
   * @return success
   */
  def removeThreadArmor(id: Int, id_armor: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
        dao.removeThreadArmor(id, id_armor) match {
          case false => BadRequest("")
          case _ =>
            dao.storeAction(routes.Charackters.
              removeThreadArmor(id: Int, id_armor: Int)
              .toString,
              id, request.user.main.userId)
            Ok("")
      }
    }

  /**
   * Add a spell to matrix
   *
   * @param id_spell
   * @param id_char
   * @return Success
   */
  def Spell2Matrix(id_spell: Int, id_char: Int) = SecuredAction {
    dao.Spell2Matrix(id_spell, id_char) match {
      case false => BadRequest("")
      case _ => Ok("")
    }
  }

  /**
   * Remove a spell from a  matrix
   *
   * @param id_spell
   * @param id_char
   * @return Success
   */
  def SpellFromMatrix(id_spell: Int, id_char: Int) = SecuredAction {
    dao.SpellFromMatrix(id_spell, id_char) match {
      case false => BadRequest("")
      case _ => Ok("")
    }
  }

  /**
   * Return the dice string corresponding to the step number
   *
   * @param step
   * @return Dice string as JSON
   */
  def getDice(step: Int) = SecuredAction {
    val dice = utilities.getDiceForStep(step)
    dice match {
      case None => BadRequest("")
      case _ => Ok(Json.toJson(JsString(dice.get)))
        .withHeaders(CACHE_CONTROL -> "no-cache",
          ETAG -> dice.get.hashCode.toString)
    }
  }

  /**
   * Return the result of rolling a dice with the indicated number of sides
   *
   * @param dice the sides of the dice
   * @return Rsult as JSON
   */
  def rollDice(dice: Int) = SecuredAction {
    val result: Int = utilities.rollDice(dice)
    Ok(Json.toJson(JsNumber(result))).withHeaders(CACHE_CONTROL -> "no-cache")
  }
  
    /**
   * Return the probability to reach value with step
   *
   * @param value
   * @param step  
   * @return Result as JSON
   */
  def getProbForValue(value: Int, step:Int) = SecuredAction {
    if (step>2 && step<101){
    val result = utilities.getProbabilityWithStep(value, step)
    Ok(Json.toJson(JsNumber(result))).withHeaders(CACHE_CONTROL -> "no-cache")
    }
    else{
      BadRequest("step not known")
    }
  }

  /**
   * Return the result of rolling dice according to the dice string
   *
   *
   * Each die starts with a number indicating how often that dice should be
   * thrown. It is fellowd by the lower case letter d which in turn is fellowed
   * by the number of sides
   *
   * @param dieces A string modeeling dices
   * @return Result as JSON
   */
  def rollDiceString(dices: String) = SecuredAction {

    try {

      if (dices contains "x") {        
    	  val pattern  = """(\d+)x(.+)""".r
          val dice_match = pattern.findFirstMatchIn(dices).get
          val result = 1.to(dice_match.group(1).toInt).map(count=>
          	if(dice_match.group(2) contains ";"){
          		  dice_match.group(2).split(";").map(sub_dices =>
		          utilities.rollDiceString(sub_dices))		          
          	} 
          	else{
          	  Array(utilities.rollDiceString(dice_match.group(2)))
          	  }          	
           )
          Ok(Json.toJson(result)).withHeaders(CACHE_CONTROL -> "no-cache",
          ETAG -> result.hashCode.toString) 
      	} 
      else if(dices contains ";"){
          val result = dices.split(";").map(sub_dices =>
          utilities.rollDiceString(sub_dices))
          Ok(Json.toJson(result)).withHeaders(CACHE_CONTROL -> "no-cache",
          ETAG -> result.hashCode.toString)
      } 
      else {
        val result: Int = utilities.rollDiceString(dices)
        Ok(Json.toJson(JsNumber(result)))
          .withHeaders(CACHE_CONTROL -> "no-cache",
            ETAG -> result.hashCode.toString)
      }
    } catch {
      case e: Exception => BadRequest("")
    }
  }

  /**
   * Redirects the user from / to /auth/login when not logged in and
   * to /chars when logged in. Thsi prevents the old firefox credentiels Bug
   *
   * @return Redirect
   */
  def redirect_user = UserAwareAction { implicit request =>
    request.user match {
      case Some(user) => Redirect("/chars/")
      case _ => Redirect("/auth/login")
    }
  }

  /**
   * Remove the current user from the users allowed to edit a character
   *
   * @param id_char
   * @return Redirect
   */
  def removeUserFromChar(id_char: Int) =
    SecuredAction(UserAllowedWithCharacterId(id_char)) { implicit request =>
      dao.removeUserFromChar(id_char, request.user.main.userId) match {
        case false => BadRequest("")
        case true => Ok("")
      }
    }

  /**
   * Share a character with a user
   *
   * @param id_char
   * @return Redirect
   */
  def shareChar(id_char: Int, user_mail: String) =
    SecuredAction(UserAllowedWithCharacterId(id_char)) { implicit request =>
      dao.shareChar(id_char, user_mail) match {
        case true => Ok("")
        case false => BadRequest("")
      }
    }

  /**
   * Buy Karma
   *
   * @param id_char
   * @param nr_points
   * @return Success
   */
  def buyKarma(id_char: Int, nr_points: Int) =
    SecuredAction(UserAllowedWithCharacterId(id_char)) { implicit request =>
        dao.buyKarma(id_char, nr_points) match {
          case true =>
            dao.storeAction(routes.Charackters.buyKarma(
              id_char: Int, nr_points: Int).toString,
              id_char, request.user.main.userId)
            Ok("")
          case false => BadRequest("")
      }
    }

  /**
   * Spent Karma
   *
   * @param id_char
   * @param nr_points
   * @return Success
   */
  def spentKarma(id_char: Int, nr_points: Int) =
    SecuredAction(UserAllowedWithCharacterId(id_char)) { implicit request =>
        dao.spentKarma(id_char, nr_points) match {
          case true =>
            dao.storeAction(routes.Charackters.spentKarma(
              id_char: Int, nr_points: Int).toString,
              id_char, request.user.main.userId)
            Ok("")
          case false => BadRequest("")
      }
    }

  /**
   * Add LP
   *
   * @param id_char
   * @param nr_points
   * @return Success
   */
  def addLP(id_char: Int, nr_points: Int) =
    SecuredAction(UserAllowedWithCharacterId(id_char)) { implicit request =>
        dao.addLP(id_char, nr_points) match {
          case true =>
            dao.storeAction(routes.Charackters.addLP(
              id_char: Int, nr_points: Int).toString,
              id_char, request.user.main.userId)
            Ok("")
          case false => BadRequest("")
      }
    }
}

case class UserAllowedWithCharacterId(char_id: Int) extends Authorization[EoceneUser] {
  def isAuthorized(user: EoceneUser, request: RequestHeader) = {
    eoceneUserService.userAllowdOnChar(user.main.userId, char_id)
  }
}
