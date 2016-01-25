/*
 * Copyright (c) 2016 Christian Garbers.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Simplified BSD License
 *  which accompanies this distribution
 *  Contributors:
 *       Christian Garbers - initial API and implementation
 */
package controllers


import eoceneServices.{EoceneUser, eoceneUserService, utilities}
import play.api.libs.json.{JsNumber, JsString, Json}
import play.api.mvc.RequestHeader
import play.mvc.Call
import securesocial.core._

import scala.util.control.NonFatal

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
      replyAndStore(dao.updateCharAttributeWithLP(id, attribute, "up"),
        routes.Charackters.improveAttributeLP(id, attribute),
        id, request.user.main.userId)
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
      replyAndStore(dao.updateCharAttributeWithLP(id, attribute, "down"),
        routes.Charackters.corruptAttributeLP(id, attribute),
        id, request.user.main.userId)
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
      replyAndStore(dao.updateCharAttributeWithPP(id, attribute, "up"),
        routes.Charackters.improveAttributePP(id, attribute),
        id, request.user.main.userId)
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
      replyAndStore(dao.updateCharAttributeWithPP(id, attribute, "down"),
        routes.Charackters.corruptAttributePP(id, attribute),
        id, request.user.main.userId)
    }

  /**
    * Change (or set) a characters Race
   *
   * @param id character id
    * @param idRace
   * @return success
   */
  def changeCharRace(id: Int, idRace: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
      replyAndStore(dao.changeCharRace(id, idRace),
        routes.Charackters.changeCharRace(id, idRace),
        id, request.user.main.userId)
    }

  /**
    * Advance the character in a given discipline
   *
   * @param id character id
    * @param idDiscipline
   * @return success
   */
  def improveCharDiscipline(id: Int, idDiscipline: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
      replyAndStore(dao.improveCharDiscipline(id, idDiscipline),
        routes.Charackters.improveCharDiscipline(id, idDiscipline),
        id, request.user.main.userId)
    }

  /**
    * DisAdvance the character in a given discipline
   *
   * @param id character id
    * @param idDiscipline
   * @return success
   */
  def corruptCharDiscipline(id: Int, idDiscipline: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
      replyAndStore(dao.corruptCharDiscipline(id, idDiscipline),
        routes.Charackters.corruptCharDiscipline(id, idDiscipline),
        id, request.user.main.userId)
    }

  /**
    * Improve the a talent
   *
   * @param id character id
    * @param idTalent
   * @return success
   */
  def improveCharTalent(id: Int, idTalent: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
      replyAndStore(dao.improveCharTalent(id, idTalent),
        routes.Charackters.improveCharTalent(id, idTalent),
        id, request.user.main.userId)
    }

  /**
   * Reduce a talent
   *
   * @param id character id
    * @param idTalent
   * @return success
   */
  def corruptCharTalent(id: Int, idTalent: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
      replyAndStore(dao.corruptCharTalent(id, idTalent),
        routes.Charackters.corruptCharTalent(id, idTalent),
        id, request.user.main.userId)
    }

  /**
    * Improve a skill (or add it)
    *
    * @param id character id
    * @param idSkill
    * @return success
    */
  def improveCharSkill(id: Int, idSkill: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
      replyAndStore(dao.improveCharSkill(id, idSkill),
        routes.Charackters.improveCharSkill(id, idSkill),
        id, request.user.main.userId)
    }

  /**
    * Reduce a skill (or remnove it)
   *
   * @param id character id
    * @param idSkill
   * @return success
   */
  def corruptCharSkill(id: Int, idSkill: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
      replyAndStore(dao.corruptCharSkill(id, idSkill),
        routes.Charackters.corruptCharSkill(id, idSkill),
        id, request.user.main.userId)
    }

  /**
    * Learn a spell
   *
   * @param id character id
    * @param idSpell
   * @return success
   */
  def learnCharSpell(id: Int, idSpell: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
      replyAndStore(dao.learnCharSpell(id, idSpell),
        routes.Charackters.learnCharSpell(id, idSpell),
        id, request.user.main.userId)
    }

  /**
    * Forget a spell
   *
   * @param id character id
    * @param idSpell
   * @return success
   */
  def unlearnCharSpell(id: Int, idSpell: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
      replyAndStore(dao.unlearnCharSpell(id, idSpell),
        routes.Charackters.unlearnCharSpell(id, idSpell),
        id, request.user.main.userId)
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
    dao.changeCharName(id, name.getOrElse("")) match {
      case false => BadRequest("")
      case _ => Ok("")
    }
  }

  /**
    * Add armor to the character
   *
   * @param id character id
    * @param idArmor
   * @return success
   */
  def getArmor(id: Int, idArmor: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
      replyAndStore(dao.getArmor(id, idArmor),
        routes.Charackters.getArmor(id, idArmor),
        id, request.user.main.userId)
    }

  /**
    * Remove armor from the character
   *
   * @param id character id
    * @param idArmor
   * @return success
   */
  def removeArmor(id: Int, idArmor: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
      replyAndStore(dao.removeArmor(id, idArmor),
        routes.Charackters.removeArmor(id, idArmor),
        id, request.user.main.userId)
    }
  
  /**
    * Attach a thread to an armor item
   *
   * @param id character id
    * @param idArmor
   * @return success
   */
  def attachThreadArmor(id: Int, idArmor: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
      replyAndStore(dao.attachThreadArmor(id, idArmor),
        routes.Charackters.attachThreadArmor(id, idArmor),
        id, request.user.main.userId)
    }

  /**
    * Remove a thread to an armor item
   *
   * @param id character id
    * @param idArmor
   * @return success
   */
  def removeThreadArmor(id: Int, idArmor: Int) =
    SecuredAction(UserAllowedWithCharacterId(id)) { implicit request =>
      replyAndStore(dao.removeThreadArmor(id, idArmor),
        routes.Charackters.removeThreadArmor(id, idArmor),
        id, request.user.main.userId)
    }

  /**
    * Add a spell to matrix
   *
    * @param idSpell
    * @param idChar
    * @return Success
   */
  def Spell2Matrix(idSpell: Int, idChar: Int) = SecuredAction {
    dao.Spell2Matrix(idSpell, idChar) match {
      case false => BadRequest("")
      case _ => Ok("")
    }
  }

  /**
    * Remove a spell from a  matrix
   *
    * @param idSpell
    * @param idChar
   * @return Success
   */
  def SpellFromMatrix(idSpell: Int, idChar: Int) = SecuredAction {
    dao.SpellFromMatrix(idSpell, idChar) match {
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
          ETAG -> dice.getOrElse {
            0
          }.hashCode.toString)
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
  def getProbForValue(value: Int, step: Int) = SecuredAction {
    if (step > 2 && step < 101) {
      val result = utilities.getProbabilityWithStep(value, step)
    Ok(Json.toJson(JsNumber(result))).withHeaders(CACHE_CONTROL -> "no-cache")
    }
    else {
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
    * @param dices A string modeeling dices
   * @return Result as JSON
   */
  def rollDiceString(dices: String) = SecuredAction {

    try {

      if (dices contains "x") {
        val pattern = """(\d+)x(.+)""".r
        val diceMatch = pattern.findFirstMatchIn(dices).get
        val result = 1.to(diceMatch.group(1).toInt).map(count =>
          if (diceMatch.group(2) contains ";") {
            diceMatch.group(2).split(";").map(subDices =>
              utilities.rollDiceString(subDices))
          }
          else {
            Array(utilities.rollDiceString(diceMatch.group(2)))
          }
        )
        Ok(Json.toJson(result)).withHeaders(CACHE_CONTROL -> "no-cache",
          ETAG -> result.hashCode.toString)
      }
      else if (dices contains ";") {
        val result = dices.split(";").map(subDices =>
          utilities.rollDiceString(subDices))
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
      case NonFatal(e) => BadRequest("")
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
    * @param idChar
   * @return Redirect
   */
  def removeUserFromChar(idChar: Int) =
    SecuredAction(UserAllowedWithCharacterId(idChar)) { implicit request =>
      dao.removeUserFromChar(idChar, request.user.main.userId) match {
        case false => BadRequest("")
        case true => Ok("")
      }
    }

  /**
    * Share a character with a user
   *
    * @param idChar
   * @return Redirect
   */
  def shareChar(idChar: Int, userMail: String) =
    SecuredAction(UserAllowedWithCharacterId(idChar)) { implicit request =>
      dao.shareChar(idChar, userMail) match {
        case true => Ok("")
        case false => BadRequest("")
      }
    }

  /**
   * Buy Karma
   *
    * @param idChar
    * @param nrPoints
   * @return Success
   */
  def buyKarma(idChar: Int, nrPoints: Int) =
    SecuredAction(UserAllowedWithCharacterId(idChar)) { implicit request =>
      replyAndStore(dao.buyKarma(idChar, nrPoints),
        routes.Charackters.buyKarma(idChar, nrPoints),
        idChar, request.user.main.userId)
    }

  def replyAndStore(result: Boolean, action: Call, idChar: Int, idUser: String) =
    result match {
      case false => BadRequest("")
      case _ =>
        dao.storeAction(action.toString,
          idChar, idUser)
        Ok("")
    }

  /**
   * Spent Karma
   *
    * @param idChar
    * @param nrPoints
   * @return Success
   */
  def spentKarma(idChar: Int, nrPoints: Int) =
    SecuredAction(UserAllowedWithCharacterId(idChar)) { implicit request =>
      replyAndStore(dao.spentKarma(idChar, nrPoints),
        routes.Charackters.spentKarma(idChar, nrPoints),
        idChar, request.user.main.userId)
    }

  /**
   * Add LP
   *
    * @param idChar
    * @param nrPoints
   * @return Success
   */
  def addLP(idChar: Int, nrPoints: Int) =
    SecuredAction(UserAllowedWithCharacterId(idChar)) { implicit request =>
      replyAndStore(dao.addLP(idChar, nrPoints),
        routes.Charackters.addLP(idChar, nrPoints),
        idChar, request.user.main.userId)
    }
}

case class UserAllowedWithCharacterId(char_id: Int) extends Authorization[EoceneUser] {
  def isAuthorized(user: EoceneUser, request: RequestHeader) = {
    eoceneUserService.userAllowdOnChar(user.main.userId, char_id)
  }
}
