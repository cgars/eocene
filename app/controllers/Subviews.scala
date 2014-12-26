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
import models.Char
import play.api.libs.json.JsString
import eoceneServices.EoceneUser
import play.api.mvc.{ Action, RequestHeader }
import securesocial.core._
import views.html.defaultpages.badRequest

/**
 * Return is responsible for handlign out propper
 * HTML
 */
class Subviews(override implicit val env: RuntimeEnvironment[EoceneUser])
  extends securesocial.core.SecureSocial[EoceneUser] {

  /**
   * Fetch a character ad prepare the Talent view
   *
   * @param id Chracters id
   * @return the talent view
   */
  def getTalentsByCharId(id: Int) = SecuredAction(UserAllowedWithCharacterId(id)) {
    DB.withConnection("chars") { implicit c =>
      val char = Char.getCharById(id)
      char match {
        case None => BadRequest("")
        case _ => Ok(views.html.talents(char.get))
      }
    }
  }
  /**
   * Fetch a View of all characters availiable and wrap them in a complete page
   *
   * @return the characters view
   */
  def showChars() = SecuredAction { implicit request =>
    DB.withConnection("chars") { implicit c =>
      val result = eoceneSqlStrings.GET_CHARS_FOR_USER.onParams(
        request.user.main.userId)().
        map(row => (row[String]("name"), row[Int]("id"))).toList
      Ok(views.html.showChars(result, request.user.main))
    }
  }
  /**
   * Fetch a View of all characters availiable
   *
   * @return the characters view
   */
  def getChars() = SecuredAction { implicit request =>
    DB.withConnection("chars") { implicit c =>
      val result = eoceneSqlStrings.GET_CHARS_FOR_USER.onParams(
        request.user.main.userId)().map(row => (row[String]("name"),
          row[Int]("id"))).toList
      Ok(views.html.chars(result))
    }
  }

  /**
   * Fetch a View of all characters availiable and wrap them in a complete page
   *
   * @return the characters view
   */
  def showChar(id: Int) = SecuredAction(UserAllowedWithCharacterId(id)) {
    implicit request =>
      DB.withConnection("chars") { implicit c =>
        val char = Char.getCharById(id)
        char match {
          case None => BadRequest("")
          case _ =>
            val validator = char.get.getValidator
            val char_view = views.html.showChar(char.get, request.user.main)
            Ok(char_view)
        }
      }
  }

  /**
   * Fetch a View of all characters availiable
   *
   * @return the characters view
   */
  def getChar(id: Int, date: String) =
    SecuredAction(UserAllowedWithCharacterId(id)) {
      DB.withConnection("chars") { implicit c =>
        val char = Char.getCharById(id)
        char match {
          case None => BadRequest("")
          case _ =>
            val char_view = views.html.char(char.get)
            Logger.debug(routes.Subviews.getChar(id, date).toString)
            Ok(char_view).withHeaders(CACHE_CONTROL -> "no-cache",
              ETAG -> char.get.hashCode.toString)
        }
      }
    }

  /**
   * Fetch the availible Races
   *
   * @return the race view
   */
  def Races() = SecuredAction {
    DB.withConnection("chars") { implicit c =>
      val races = utilities.getRaces()
      Ok(views.html.races(races))
    }
  }

  /**
   * Fetch a View of all disciplines availiable
   *
   * @return the discipline view
   */
  def Disciplines() = SecuredAction {
    DB.withConnection("chars") { implicit c =>
      val disciplines = utilities.getDisciplines()
      Ok(views.html.disciplines(disciplines))
    }
  }

  /**
   * Fetch a View of all spells availiable
   *
   * @return the spell view
   */
  def Spells() = SecuredAction {
    DB.withConnection("chars") { implicit c =>
      val spells = utilities.getSpells()
      Ok(views.html.spells(spells))
    }
  }

  /**
   * Fetch a View of all spells availiable for a character
   *
   * @return the spells view
   */
  def SpellsForChar(id: Int) = SecuredAction(UserAllowedWithCharacterId(id)) {
    DB.withConnection("chars") { implicit c =>
      val spells = utilities.getSpellsForChar(id)
      Ok(views.html.spells(spells)).withHeaders(CACHE_CONTROL -> "no-cache")
    }
  }

  /**
   * Fetch a View of all skills availiable
   *
   * @return the skills view
   */
  def Skills() = SecuredAction {
    DB.withConnection("chars") { implicit c =>
      val skills = utilities.getSkills()
      Ok(views.html.skills(skills))
    }
  }

  /**
   * Fetch a View of all armor availiable
   *
   * @return the armor view
   */
  def Armors() = SecuredAction {
    DB.withConnection("chars") { implicit c =>
      val armors = utilities.getArmors()
      Ok(views.html.armors(armors))
    }
  }

  /**
   * Fetch a View for the Dice Roller view
   *
   * @return the dice roller view
   */
  def Dice(target_class: String, target_id: String, dice: String, char_id: Int) =
    SecuredAction(UserAllowedWithCharacterId(char_id)) {
      DB.withConnection("chars") { implicit c =>
        val char = Char.getCharById(char_id)
        char match {
          case None => BadRequest("")
          case _ => Ok(views.html.dice(char.get, target_class, target_id, dice))
            .withHeaders(CACHE_CONTROL -> "no-cache",
              ETAG -> char.get.hashCode.toString)
        }
      }
    }

  /**
   * Fetch the Spell Caster view
   *
   * @return the spell caster view
   */
  def Spell(id_spell: Int, char_id: Int) =
    SecuredAction(UserAllowedWithCharacterId(char_id)) {
      DB.withConnection("chars") { implicit c =>
        val char = Char.getCharById(char_id)
        char match {
          case None => BadRequest("")
          case _ =>
            try {
              val spell = char.get.spells.filter(spell => spell.id == id_spell)
                .head
              val spells_discipline_name = char.get.disciplines.
                filter(discipline => discipline.id == spell.id_disciline.get)
                .head.
                name
              val thread_weaving = char.get.talents.
                filter(talent => talent.name contains "Thread").
                filter(talent => talent.name contains spells_discipline_name.
                  substring(1, 5)).
                head
              val spellcasting = char.get.talents.
                filter(talent => talent.name contains "Spellcasting").head
              val effect_step: Option[Int] =
                if (spell.effect.contains("Willforce")) {
                  try {
                    val bonus: Int = spell.effect.split("\\+")(1).trim
                      .toInt
                    val willforce: Int = char.get.talents.
                      filter(talent => talent.name.contains("Wilforce"))
                      .map(talent => talent.step.getOrElse(0)).headOption.
                      getOrElse(0)
                    val willpower: Int = char.get.derived("wil_step")
                      .asInstanceOf[Int]
                    Some(bonus + willforce + willpower)

                  } catch {
                    case e: Exception => {
                      Logger.error(
                        """Error when getting the Effect for a 
			    	    			Spell view.Error was:%s""".format(e))
                      None
                    }
                  }
                } else None
              val effect_dice = utilities.getDiceForStep(effect_step.
                getOrElse(0))

              Ok(views.html.castSpell(char.get, spell, thread_weaving,
                spellcasting, effect_dice))
                .withHeaders(CACHE_CONTROL -> "no-cache",
                  ETAG -> char.get.hashCode.toString)
            } catch {
              case e: Throwable => {
                Logger.debug(e.toString())
                BadRequest("")
              }
            }
        }
      }
    }

  /**
   * Fetch share view
   *
   * @param id_char
   * @return view
   */
  def Share(char_id: Int) =
    SecuredAction(UserAllowedWithCharacterId(char_id)) {
      Ok(views.html.share(char_id)).
        withHeaders(CACHE_CONTROL -> "no-cache")
    }
  
  /**
   * Fetch the map view
   *
   * @param id_char
   * @return view
   */
  def Map() =
    SecuredAction() {implicit request =>
      Ok(views.html.map(request.user.main)).
        withHeaders(CACHE_CONTROL -> "no-cache")
    }
}
