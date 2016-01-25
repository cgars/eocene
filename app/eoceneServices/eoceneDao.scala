/*
 * Copyright (c) 2016 Christian Garbers.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Simplified BSD License
 *  which accompanies this distribution
 *  Contributors:
 *       Christian Garbers - initial API and implementation
 */

package eoceneServices

import models.{Character, Discipline, Race, Skill}

trait eoceneDao {
  def createCharByName(name: String)(implicit user: eoceneServices.EoceneUser): Option[Int]
  def getRaces(): List[Race]
  def getDisciplines(): List[Discipline]
  def getSpells(): List[Map[String, Any]]
  def getSkills(): List[Skill]
  def getRaceById(id: Int): Option[Race]
  def getCharById(id: Int): Option[Character]
  def getCharIdByName(name: String): Option[Int]

  def changeCharRace(id: Int, idRace: Int): Boolean
  def getRaceIdByCharId(id: Int): Option[Int]
  def getCharAttribute(id: Int, attribute: String): Option[Int]

  def updateCharAttribute(id: Int, attribute: String, newValue: Int): Boolean
  def updateCharAttributeWithPP(id: Int, attribute: String, direction: String): Boolean
  def updateCharAttributeWithLP(id: Int, attribute: String, direction: String): Boolean
  def getCharDisciplineRowsByCharId(id: Int): List[List[Int]]

  def improveCharDiscipline(id: Int, idDiscipline: Int): Boolean

  def corruptCharDiscipline(id: Int, idDiscipline: Int): Boolean
  def getCharTalentRowsIdByCharId(id: Int): List[List[Int]]

  def getTalentCircleByTalenAndDisciId(idTalent: Int, idChar: Int): Int

  def improveCharTalent(id: Int, idTalent: Int): Boolean

  def corruptCharTalent(id: Int, idTalent: Int): Boolean
  def getCharSkillRowsByCharId(id: Int): List[List[Int]]

  def improveCharSkill(id: Int, idSkill: Int): Boolean

  def corruptCharSkill(id: Int, idSkill: Int): Boolean
  def getCharSpellListByCharId(id: Int): List[List[Int]]

  def learnCharSpell(id: Int, idSpell: Int): Boolean

  def unlearnCharSpell(id: Int, idSpell: Int): Boolean
  def changeCharName(id: Int, name: String): Boolean

  def getArmor(id: Int, idArmor: Int): Boolean

  def removeArmor(id: Int, idArmor: Int): Boolean

  def attachThreadArmor(id: Int, idArmor: Int): Boolean

  def removeThreadArmor(id: Int, idArmor: Int): Boolean

  def Spell2Matrix(idSpell: Int, idChar: Int): Boolean

  def SpellFromMatrix(idSpell: Int, idChar: Int): Boolean

  def removeUserFromChar(idChar: Int, idUser: String): Boolean

  def shareChar(idChar: Int, userMail: String): Boolean

  def buyKarma(idChar: Int, nrPoints: Int): Boolean

  def spentKarma(idChar: Int, nrPoints: Int): Boolean

  def addLP(idChar: Int, nrPoints: Int): Boolean
  def storeAction(call: String, id_char: Int, id_user: String): Boolean
}