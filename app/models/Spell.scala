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
package models

import play.api.libs.json._


case class Spell(val id: Int, val name: String, val circle: Int, val threads: Int,
                 val weavingDiff: Int, val reatunging: Int,
                 val range: String, val duration: String, val effect: String,
                 val difficulty: String, val discription: String,
                 val idChar: Option[Int], val idDiscipline: Option[Int],
                 val spellMatrix: Option[Int])

object Spell {
  implicit val spellWrites = new Writes[Spell] {
    def writes(spell: Spell) = spell.idChar match {
      case None => JsObject(Seq("id" -> JsNumber(spell.id),
        "name" -> JsString(spell.name),
        "circle" -> JsNumber(spell.circle),
        "threads" -> JsNumber(spell.threads),
        "weaving_diff" -> JsNumber(spell.weavingDiff),
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
        "weaving_difficulty" -> JsNumber(spell.weavingDiff),
        "reatuning" -> JsNumber(spell.reatunging),
        "range" -> JsString(spell.range),
        "duration" -> JsString(spell.duration),
        "effect" -> JsString(spell.effect),
        "difficulty" -> JsString(spell.difficulty),
        "description" -> JsString(spell.discription),
        "id_char" -> JsNumber(spell.idChar.get)))

    }
  }

  def getSpell(id: Int, name: String, circle: Int, threads: Int,
               weavingDifficulty: Int, reatunging: Int,
               range: String, duration: String, effect: String,
               difficulty: String, discription: String, id_char: Option[Int],
               idDiscipline: Option[Int], spellMatrix: Option[Int]) =
    {
      Spell(id, name, circle, threads, weavingDifficulty, reatunging,
        range, duration, effect, difficulty, discription, id_char,
        idDiscipline, spellMatrix)
    }
}
