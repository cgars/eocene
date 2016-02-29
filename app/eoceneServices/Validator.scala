/*
 * Copyright (c) 2016 Christian Garbers.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Simplified BSD License
 *  which accompanies this distribution
 *  Contributors:
 *       Christian Garbers - initial API and implementation
 */

package eoceneServices

class Validator(char: models.Character) {
  val circleRequiremnents = Map(2 ->(5, 2, 1),
    3 -> (6, 3, 2),
    4 -> (7, 4, 3),
    5 -> (8, 5, 4),
    6 -> (9, 6, 5),
    7 -> (10, 7, 6),
    8 -> (11, 8, 7),
    9 -> (12, 9, 8),
    10 -> (13, 10, 9),
    11 -> (14, 11, 10),
    12 -> (15, 11, 11),
    13 -> (16, 12, 12),
    14 -> (17, 12, 13),
    15 -> (18, 13, 14))
  var message: String = ""

  def validate(): Boolean = {
    if (char.disciplines.isEmpty) {
      message.concat("No Discipline selected")
    }
    List(char.disciplines.map(discipline => checkDisciplineCircleRequirements(discipline)).
      foldLeft(true)((a1, a2) => a1 && a2),
      checkAtrributeImprovements,
      checkWindlingStrength,
      checkObsidimanStrength,
      checkTrollStrength,
      checkTrollToughness).reduce((a1,a2)=> a1&&a2)
  }

  def checkTrollToughness() = {
    if (char.race.name.equals("Troll"))
      char.derived("tou").asInstanceOf[Int]>10 match{
        case false => {
          message += "Minimum Toughness of 11 required\n"
        			  false
        			 }
        case true => true
      }
    else true
  }

  def checkTrollStrength() = {
    if (char.race.name.equals("Troll"))
      char.derived("str").asInstanceOf[Int]>10 match{
        case false => {
          message += "Minimum Strength is 11\n"
        			  false
        			 }
        case true => true
      }
    else true
  }

  def checkObsidimanStrength() = {
    if (char.race.name.equals("Obsidiman"))
      char.derived("str").asInstanceOf[Int]>14 match{
        case false => {
          message += "Minimum Strength of 15 required\n"
        			  false
        			 }
        case true => true
      }
    else true
  }

  def checkWindlingStrength() = {
    if (char.race.name.equals("Windling"))
      char.derived("str").asInstanceOf[Int]<12 match{
        case false => {
          message += "Maximum Strength is 11\n"
        			  false
        			 }
        case true => true
      }
    else true
  }

  def checkDisciplineCircleRequirements(discipline: models.Discipline): Boolean = {
    if (discipline.circle.getOrElse(0) == 1) true
    else {
      (2).to(discipline.circle.getOrElse(0)).
        map(circle => eligableForCircle(circle, discipline)).
        reduce((a1, a2) => a1 && a2)
    }
  }

  def eligableForCircle(circle: Int, discipline: models.Discipline): Boolean = {
    val talentsOdiscipline = char.talents.
      filter(talent => talent.step.isDefined).
      filter(talent => talent.disciplineId.getOrElse(0) == discipline.id)
    val nrTalents = talentsOdiscipline.size >= circleRequiremnents(circle)._1
    val minRank = talentsOdiscipline.count(talent => talent.step.getOrElse(0) >= circle) >= circleRequiremnents(circle)._2
    val singleTalent = talentsOdiscipline.
      filter(p = talent => talent.circle.getOrElse(0) > circle - 2).
      exists(talent => talent.step.getOrElse(0) >= circleRequiremnents(circle)._3)
    if (!nrTalents) {
      message += "\nRequired: A minimum of %s %s talents is required to be %s in circle %s".
        format(circleRequiremnents(circle)._1, discipline.name, discipline.name, circle)
    }
    if (!minRank) {
      message += "\nRequired: A minimum of %s %s Talents of rank %s is required to be %s in circle %s".
        format(circleRequiremnents(circle)._2, discipline.name, circle, discipline.name, circle)
    }
    if (!singleTalent) {
      message += "\nRequired: One %s talents from circle %s with at least rank %s is required to be %s in circle %s".
        format(discipline.name, circle - 1, circleRequiremnents(circle)._3, discipline.name, circle)
    }
    nrTalents && minRank && singleTalent
  }

  def checkAtrributeImprovements(): Boolean = {
    var allowed = 0
    if (char.disciplines.nonEmpty) {
      allowed += char.disciplines.head.circle.getOrElse(0) - 1
      char.disciplines.takeRight(char.disciplines.size - 1)
        .foreach(discipline => allowed += discipline.circle.getOrElse(0) / 2)
    }

    val improvements = char.dexLevel + char.chaLevel + char.perLevel +
      char.willLevel + char.touLevel + char.strLevel
    if (improvements > allowed) {
      message += "You have improved to many atrributes\n"
      false
    } else true

  }
}

object Validator {

  def getValidator(char: models.Character): eoceneServices.Validator = {
    val validator = new Validator(char)
    validator
  }

}
