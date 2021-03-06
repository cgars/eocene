/*
 * Copyright (c) 2016 Christian Garbers.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Simplified BSD License
 *  which accompanies this distribution
 *  Contributors:
 *       Christian Garbers - initial API and implementation
 */

package controllers

import javax.inject.Inject

import eoceneServices.{eoceneEnvironment, utilities}
import play.api._

import scala.util.Try
import scala.util.control.NonFatal

/**
 * Return is responsible for handlign out propper
 * HTML
 */
class Subviews @Inject()(override implicit val env: eoceneEnvironment,
                         dao:eoceneServices.eoceneSqlService)
  extends securesocial.core.SecureSocial {
  	
  /**
   * Fetch a character ad prepare the Talent view
   *
   * @param id Chracters id
   * @return the talent view
   */
  def getTalentsByCharId(id: Int) = SecuredAction(UserAllowedWithCharacterId(id)) {
      val char = dao.getCharById(id)
      char match {
        case None => BadRequest("")
        case _ => Ok(views.html.talents(char.get))
    }
  }

  /**
   * Fetch a View of all characters availiable and wrap them in a complete page
   *
   * @return the characters view
   */
  def showChars() = SecuredAction { implicit request =>
      val result = dao.getCharsForUser(request.user.main.userId)
      Ok(views.html.showChars(result, request.user.main))
  }
  /**
   * Fetch a View of all characters availiable
   *
   * @return the characters view
   */
  def getChars() = SecuredAction { implicit request =>
    val result = dao.getCharsForUser(request.user.main.userId)
    Ok(views.html.chars(result))
    }

  /**
   * Fetch a View of all characters availiable and wrap them in a complete page
   *
   * @return the characters view
   */
  def showChar(id: Int) = SecuredAction(UserAllowedWithCharacterId(id)) {
    implicit request =>
        val char = dao.getCharById(id)
        char match {
          case None => BadRequest("")
          case Some(c) =>
            val validator = c.getValidator
            val charView = views.html.showChar(c, request.user.main)
            Ok(charView)
      }
  }

  /**
   * Fetch a View of all characters availiable
   *
   * @return the characters view
   */
  def getChar(id: Int, date: String) =
    SecuredAction(UserAllowedWithCharacterId(id)) {
        val char = dao.getCharById(id)
        char match {
          case None => BadRequest("")
          case _ =>
            val charView = views.html.char(char.get)
            Logger.debug(routes.Subviews.getChar(id, date).toString)
            Ok(charView).withHeaders(CACHE_CONTROL -> "no-cache",
              ETAG -> char.get.hashCode.toString)
      }
    }

  /**
   * Fetch the availible Races
   *
   * @return the race view
   */
  def Races() = SecuredAction {
      val races = dao.getRaces()
      Ok(views.html.races(races))
  }

  /**
   * Fetch a View of all disciplines availiable
   *
   * @return the discipline view
   */
  def Disciplines() = SecuredAction {
      val disciplines = dao.getDisciplines()
      Ok(views.html.disciplines(disciplines))
  }

  /**
   * Fetch a View of all spells availiable
   *
   * @return the spell view
   */
  def Spells() = SecuredAction {
      val spells = dao.getSpells()
      Ok(views.html.spells(spells))
  }

  /**
   * Fetch a View of all spells availiable for a character
   *
   * @return the spells view
   */
  def SpellsForChar(id: Int) = SecuredAction(UserAllowedWithCharacterId(id)) {
      val spells = dao.getSpellsForChar(id)
      Ok(views.html.spells(spells)).withHeaders(CACHE_CONTROL -> "no-cache")
  }

  /**
   * Fetch a View of all skills availiable
   *
   * @return the skills view
   */
  def Skills() = SecuredAction {
      val skills = dao.getSkills()
      Ok(views.html.skills(skills))
  }

  /**
   * Fetch a View of all armor availiable
   *
   * @return the armor view
   */
  def Armors() = SecuredAction {
      val armors = dao.getArmors()
      Ok(views.html.armors(armors))
  }

  /**
   * Fetch a View for the Dice Roller view
   *
   * @return the dice roller view
   */
  def Dice(targetClass: String, targetId: String, dice: String, charId: Int) =
    SecuredAction(UserAllowedWithCharacterId(charId)) {
      val char = dao.getCharById(charId)
        char match {
          case None => BadRequest("")
          case _ => Ok(views.html.dice(char.get, targetClass, targetId, dice))
            .withHeaders(CACHE_CONTROL -> "no-cache",
              ETAG -> char.get.hashCode.toString)
      }
    }

  /**
   * Fetch the Spell Caster view
   *
   * @return the spell caster view
   */
  def Spell(idSpell: Int, charId: Int) =
    SecuredAction(UserAllowedWithCharacterId(charId)) {
        val char = dao.getCharById(charId)
        char match {
          case None => BadRequest("")
          case Some(c) =>
            try {
              val spell = c.spells.filter(spell => spell.id == idSpell)
                .head
              val spellsDisciplineName = c.disciplines.
                filter(discipline => discipline.id == spell.idDiscipline.get)
                .head.
                name
              val threadWeaving = c.talents.
                filter(talent => talent.name contains "Thread").
                filter(talent => talent.name contains spellsDisciplineName.
                  substring(1, 5)).
                head
              val spellcasting = c.talents.
                filter(talent => talent.name contains "Spellcasting").head
              val effectStep: Option[Int] = {
                val bonus: Int = Try(spell.effect.split("\\+").lift(1).getOrElse("0").trim
                  .toInt).toOption.getOrElse(0)
                val willforce: Int = c.talents.
                  filter(talent => talent.name.contains("Wilforce"))
                  .map(talent => talent.step.getOrElse(0)).headOption.
                  getOrElse(0)
                val willpower: Int = c.derived("wil_step")
                  .asInstanceOf[Int]
                Some(bonus + willforce + willpower)
              }

              val effectDice = utilities.getDiceForStep(effectStep.
                getOrElse(0))

              Ok(views.html.castSpell(c, spell, threadWeaving,
                spellcasting, effectDice))
                .withHeaders(CACHE_CONTROL -> "no-cache",
                  ETAG -> c.hashCode.toString)
            } catch {
              case NonFatal(e)=> {
                Logger.debug(e.toString())
                BadRequest("")
            }
        }
      }
    }

  /**
   * Fetch share view
   *
    * @param charId
   * @return view
   */
  def Share(charId: Int) =
    SecuredAction(UserAllowedWithCharacterId(charId)) {
      Ok(views.html.share(charId)).
        withHeaders(CACHE_CONTROL -> "no-cache")
    }

  /**
   * Fetch the map view
   *
   * @return view
   */
  def Map() =
    SecuredAction() { implicit request =>
      Ok(views.html.map(request.user.main)).
        withHeaders(CACHE_CONTROL -> "no-cache")
    }
}
