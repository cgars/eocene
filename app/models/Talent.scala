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


case class Talent(val id: Int, val name: String, val action: Boolean,
                  val karma: Boolean, val strain: String, val formula: String, val step: Option[Int],
                  val disciplined: Option[Boolean], val circle: Option[Int],
                  val disciplineId: Option[Int]) {

  /**
    * Return the Dice for this Talent
    *
    * @param char The Character who has this talent
    * @return The Dice String for this talent
    */
  def getDice(char: Character) = eoceneServices.utilities.getDiceForStep(getRank(char))

  /**
   * Returns the Rank the talent currently has
   *
   * This methods checks for the attributed as defined in the talent formula
   * and if neccesary (according to the formula) it adds a modifier.
   *
   * @param char The Character who has this talent
   * @return The rank of this talent or 0
   */
  def getRank(char: Character) = {
    val formulaSplit = formula.split("\\+") // Split the formula (for cases like Dex+3)
    step match {
      case None => 0 //in case we have no step rank is not defined
      case _ => //First we do the formula attribute conversion
        val rank = formulaSplit(0) match {
          case "Dex" => char.derived("dex_step").asInstanceOf[Int] + step.getOrElse(0)
          case "Str" => char.derived("str_step").asInstanceOf[Int] + step.getOrElse(0)
          case "Tou" => char.derived("tou_step").asInstanceOf[Int] + step.getOrElse(0)
          case "Cha" => char.derived("cha_step").asInstanceOf[Int] + step.getOrElse(0)
          case "Per" => char.derived("dex_step").asInstanceOf[Int] + step.getOrElse(0)
          case "Wil" => char.derived("wil_step").asInstanceOf[Int] + step.getOrElse(0)
          case _ => 0
        }
        //return the derived rank or add the second part of formula
        formulaSplit.size match {
          case 1 => rank
          case _ => rank + formulaSplit(1).toInt
        }
    }
  }

}

object Talent {
  implicit val talentWrites = new Writes[Talent] {
    def writes(talent: Talent) = talent.step match {
      case None => JsObject(Seq())
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

  def getTalent(id: Int, name: String, action: Boolean, karma: Boolean, strain: String,
                formula: String, step: Int, disciplined: Boolean, circle: Int, disciplineId: Int) = {
    Talent(id: Int, name: String, action: Boolean, karma: Boolean, strain: String,
      formula: String, Option(step), Option(disciplined), Some(circle), Option(disciplineId))
  }
}
