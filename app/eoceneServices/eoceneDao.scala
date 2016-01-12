package eoceneServices

import models.Race
import models.Discipline
import models.Spell
import models.Skill
import models.Character

trait eoceneDao {
	def createCharByName(name:String)(implicit user: eoceneServices.EoceneUser):Option[Int]
	def getRaces():List[Race]
	def getDisciplines():List[Discipline]
	def getSpells():List[Map[String,Any]]
	def getSkills():List[Skill]
	def getRaceById(id: Int):Option[Race] 
	def getCharById(id: Int):Option[Character]
	def getCharIdByName(name:String):Option[Int]
	def changeCharRace(id: Int, id_race: Int): Boolean
	def getRaceIdByCharId(id: Int): Option[Int]
	def getCharAttribute(id: Int, attribute: String): Option[Int]
    def updateCharAttribute(id: Int, attribute: String, new_value: Int): Boolean
    def updateCharAttributeWithPP(id: Int, attribute: String, direction: String): Boolean
    def updateCharAttributeWithLP(id: Int, attribute: String, direction: String): Boolean 
    def getCharDisciplineRowsByCharId(id: Int): List[List[Int]]
    def improveCharDiscipline(id: Int, id_discipline: Int): Boolean
    def corruptCharDiscipline(id: Int, id_discipline: Int): Boolean
    def getCharTalentRowsIdByCharId(id: Int): List[List[Int]] 
    def getTalentCircleByTalenAndDisciId(id_talent: Int, id_char: Int): Int
    def improveCharTalent(id: Int, id_talent: Int): Boolean
    def corruptCharTalent(id: Int, id_talent: Int): Boolean 
    def getCharSkillRowsByCharId(id: Int): List[List[Int]] 
    def improveCharSkill(id: Int, id_skill: Int): Boolean
    def corruptCharSkill(id: Int, id_skill: Int): Boolean
    def getCharSpellListByCharId(id: Int): List[List[Int]]
    def learnCharSpell(id: Int, id_Spell: Int): Boolean
    def unlearnCharSpell(id: Int, id_Spell: Int): Boolean
    def changeCharName(id: Int, name: String): Boolean
    def getArmor(id: Int, id_armor: Int): Boolean
    def removeArmor(id: Int, id_armor: Int): Boolean
    def attachThreadArmor(id: Int, id_armor: Int): Boolean
    def removeThreadArmor(id: Int, id_armor: Int): Boolean
    def Spell2Matrix(id_spell: Int, id_char: Int): Boolean
    def SpellFromMatrix(id_spell: Int, id_char: Int): Boolean
    def removeUserFromChar(id_char: Int, id_user: String): Boolean
    def shareChar(id_char: Int, user_mail: String): Boolean
    def buyKarma(id_char: Int, nr_points: Int): Boolean
    def spentKarma(id_char: Int, nr_points: Int): Boolean
    def addLP(id_char: Int, nr_points: Int): Boolean
    def storeAction(call: String, id_char: Int, id_user: String):Boolean
}