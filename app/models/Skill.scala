/*
 * Copyright (c) 2016 Christian Garbers.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Simplified BSD License
 *  which accompanies this distribution
 *  Contributors:
 *       Christian Garbers - initial API and implementation
 */

package models


import play.api.libs.json._


case class Skill(val id: Int, val name: String, val formula: String,
                 val skillType: String, val comm: String, step: Option[Int]) {

  /**
    * Return the Dice for this Skill
   *
   * @param char The Character who has this skill
    * @return The Dice String for this skill
   */
  def getDice(char: Character) = eoceneServices.utilities.getDiceForStep(getRank(char))

  /**
    * Returns the Rank the skill currently has
   *
    * This methods checks for the attributed as defined in the skill formula
    * and if neccesary (according to the formula) it adds a modifier.
    *
   * @param char The Character who has this skill
    * @return The rank of this skill or 0
   */
  def getRank(char: Character) = {
    val formulaSplit = formula.split("\\+") // Split the formula (for cases like Dex+3)
    step match {
      case None => 0 //in case we have no step rank is not defined
      case _ => //First we do the formula attribute conversion
        val rank = char.derived(formulaSplit(0).toLowerCase() + "_step")
          .asInstanceOf[Int] + step.getOrElse(0)
        //return the derived rank or add the second part of formula
        formulaSplit.size match {
          case 1 => rank
          case _ => rank + formulaSplit(1).toInt
        }
    }
  }

}

object Skill {
  implicit val skillWrites = new Writes[Skill] {
    def writes(skill: Skill) = skill.step match {
      case None => JsObject(Seq())
      case _ => JsObject(Seq("id" -> JsNumber(skill.id),
        "name" -> JsString(skill.name),
        "formula" -> JsString(skill.formula),
        "skill_type" -> JsString(skill.skillType),
        "comm" -> JsString(skill.comm),
        "step" -> JsNumber(skill.step.get)))

    }
  }

  def getSkill(id: Int, name: String, formula: String, skillType: String,
               comm: String, step: Option[Int]) = Skill(id, name, formula, skillType, comm, step)
}
