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

case class Spell(val id: Int, val name: String, val circle: Int, val threads: Int,
  val weaving_diff: Int, val reatunging: Int,
  val range: String, val duration: String, val effect: String,
  val difficulty: String, val discription: String,
  val id_char: Option[Int],val id_disciline: Option[Int],
  val spell_matrix: Option[Int])

object Spell {
  
  /**
   * Get a Spell build from row
   * 
   * @param rows the rows from a db call 
   * @return Spell
   */ 
  def getSpellByRow(row: anorm.Row, step: Int = 0, circle: Int = 1, 
      disciplined: Boolean = false) = {
	  Spell(row[Int]("id"), row[String]("name"), row[Int]("Spells.circle"), 
	      row[Int]("threads"),
      row[Int]("weaving_diff"), row[Int]("reatuning"),
      row[String]("runge"), row[String]("duration"),
      row[String]("effect"), row[String]("difficulty"),
      row[String]("description"), row[Option[Int]]("id_char"),
      row[Option[Int]]("id_discipline"),
      row[Option[Int]]("spell_matrix"))
  }
  
  /**
   * Get a Spell by its id
   * 
   * @param id
   * @return Spell
   */ 
  def getSpellById(id: Int, step: Int = 0, circle: Int = 1, disciplined: 
      Boolean = false): Spell = {
    DB.withConnection("chars") { implicit c =>
      val querry = SQL(eoceneSqlStrings.GET_SPELL_BY_ID).onParams(id)()
      getSpellByRow(querry.head, step, circle, disciplined)
    }
  }

  def getSpell(id: Int, name: String, circle: Int, threads: Int,
    weaving_difficulty: Int, reatunging: Int,
    range: String, duration: String, effect: String,
    difficulty: String, discription: String, id_char: Option[Int],
    id_discipline:Option[Int],  spell_matrix:Option[Int]) =
    {
      Spell(id, name, circle, threads, weaving_difficulty, reatunging,
        range, duration, effect, difficulty, discription, id_char, 
        id_discipline, spell_matrix)

    }
  
  /**
   * Get all Spells that belong to a discipline
   * 
   * @param id discipline id
   * @return A List of Spells
   */ 
  def getSpellWithDisciplineAsMap(row: anorm.Row) = {
    Map("spell_name" -> row[String]("Spells.name"),
      "discipline_name" -> row[String]("Disciplines.name"),
      "circle" -> row[Int]("Spells.circle"),
      "id" -> row[Int]("Spells.id"))
  }

  implicit object TalentFormat extends Format[Spell] {

    def reads(json: JsValue) = JsSuccess(getSpellById(1))

    def writes(spell: Spell) = spell.id_char match {
      case None => JsObject(Seq("id" -> JsNumber(spell.id),
        "name" -> JsString(spell.name),
        "circle" -> JsNumber(spell.circle),
        "threads" -> JsNumber(spell.threads),
        "weaving_diff" -> JsNumber(spell.weaving_diff),
        "reatuning" -> JsNumber(spell.reatunging),
        "range" -> JsString(spell.range),
        "duration" -> JsString(spell.duration),
        "effect" -> JsString(spell.effect),
        "difficulty" -> JsString(spell.difficulty),
        "description" -> JsString(spell.discription),
        "id_char" -> JsNumber(0)))

      case _ => JsObject(Seq("id" -> JsNumber(spell.id),
        "name" -> JsString(spell.name),
        "circle" -> JsNumber(spell.circle),
        "threads" -> JsNumber(spell.threads),
        "weaving_difficulty" -> JsNumber(spell.weaving_diff),
        "reatuning" -> JsNumber(spell.reatunging),
        "range" -> JsString(spell.range),
        "duration" -> JsString(spell.duration),
        "effect" -> JsString(spell.effect),
        "difficulty" -> JsString(spell.difficulty),
        "description" -> JsString(spell.discription),
        "id_char" -> JsNumber(spell.id_char.get)))

    }
  }
}
