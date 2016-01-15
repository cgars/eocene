package eoceneServices

import anorm._
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import models.{Armor, Character, Discipline, Modifier, Power, Race, Skill, Spell, Talent}
import play.Logger
import play.api.Play.current
import play.api.db.DB


class eoceneSqlService extends eoceneDao{
  
   /**
   * Get all availible races
   *
   * @return List[Race]
   */
  def getRaces() = {
	  DB.withConnection("chars") { implicit c =>
      eoceneSqlStrings.GET_RACES().map(row => this.getRaceById(row[Int]("id")).get).toList
      .sortWith((a1, a2) => a1.name < a2.name)
	  }
  }
  
  /**
    * Get a Race By id
   *
   * @param id
    * @return Race
   */
  def getRaceById(id: Int): Option[Race] = {
    DB.withConnection("chars") { implicit c =>
      val querry = SQL(eoceneSqlStrings.GET_RACE_BY_ID).onParams(id)()
      querry.size match {
        case 0 => None
        case _ =>
          this.getRaceWithRow(querry.head)
      }
    }
  }
  
  /**
   * Get a Discipline
   *
   * @param id
   * @return Discipline
   */
  def getDisciplineById(id: Int, circle: Int = 0): Discipline = {
    DB.withConnection("chars") { implicit c =>
      val querry = SQL(eoceneSqlStrings.GET_DISCIPLINE_BY_ID).onParams(id)().
        groupBy(row => row[Int]("Disciplines.id"))
      getDisciplineByRow(querry(0))
    }
  }

  /**
   * Get a represenation of the talents_discipline table with all talents not
   * higher than a certain circle
   *
   * @param id character id
   * @param circle
   * @return Discipline
   */
  def getTalentsByDisciplinesId(id: Int, circle: Int): List[List[AnyVal]] = {
    DB.withConnection("chars") { implicit c =>
      val querry = SQL(eoceneSqlStrings.GET_TALENT_ID_BY_DISCIPLINE_ID.format(id, circle))()
      return querry.map(x => List(x[Int]("id_talent"),
        x[Int]("id_discipline"),
        x[Boolean]("disciplined"),
        x[Int]("circle"))).toList
    }
  }
  
  /**
   * Return the Talent with id
   *
   * @param row: A row from a db querry
   * @return Talent
   */
  def getTalentById(id: Int, step: Int = 0, circle: Int = 1,
    disciplined: Boolean = false): Talent = {
    DB.withConnection("chars") { implicit c =>
	    val querry = eoceneSqlStrings.TALENT_JOIN.onParams(id)()
	    getTalentByRow(querry.head)
    }
  }

  /**
    * Get a Spell by its id
    *
    * @param id
    * @return Spell
    */
  def getSpellById(id: Int, step: Int = 0, circle: Int = 1, disciplined: Boolean = false): Spell = {
    DB.withConnection("chars") { implicit c =>
      val querry = SQL(eoceneSqlStrings.GET_SPELL_BY_ID).onParams(id)()
      getSpellByRow(querry.head, step, circle, disciplined)
    }
  }

  /**
    * Get a Skill by its id
   *
   * @param id
    * @return Discipline
   */
  def getSkillById(id: Int): Skill = {
    DB.withConnection("chars") { implicit c =>
      val querry = SQL(eoceneSqlStrings.GET_SKILL_BY_ID).onParams(id)()
      getSkillByRow(querry(0))
    }
  }

  /**
    * Get a Armor
   * @param id
    * @return Armor
   */
  def getArmorById(id: Int) = {
    DB.withConnection("chars") { implicit c =>
      val querry = eoceneSqlStrings.GET_ARMOR.onParams(id)().
        groupBy(row => row[Int]("Armors.id"))
      getArmorByRows(querry.head._2)
    }
  }

 /**
   * Create a character with a name. ensure race
   *
   * @param name
   * @param user the User object that models the apllication user. Comes from
   * 			 the controller.
   * @return the id of the new character
   */
  def createCharByName(name: String)(implicit user: eoceneServices.EoceneUser) =
    {
      DB.withTransaction("chars") { implicit c =>
        val result: Boolean = SQL(eoceneSqlStrings.INSERT_CHAR).onParams(name).
          executeUpdate() > 0
        val char_id = getCharIdByName(name)
        eoceneSqlStrings.INSERT_CHARS_USERS.onParams(char_id,
          user.main.userId).execute()
        eoceneUserService.updateUsersChars()
        char_id
      }
    }

  /**
    * Get the id of a character with a certain name
    *
    * @param name
    * @return The first chharacter with that name or None
    */
  def getCharIdByName(name: String) = {
    DB.withConnection("chars") { implicit c =>
      val querry = SQL(eoceneSqlStrings.GET_CHAR_ID_FROM_NAME).onParams(name)()
      querry.headOption match {
        case None => None
        case Some(row) => row[Option[Int]]("id")
      }
    }
  }

  /**
   * Change the race of an character
   *
   * @param id the id of the character
   * @param id_race
   * @param c a sql connection. comes from the controller
   * @return Succes indicator
   */
  def changeCharRace(id: Int, id_race: Int): Boolean = {
    DB.withConnection("chars"){implicit c =>
	    getRaceIdByCharId(id) match {
	      case None => SQL(eoceneSqlStrings.SET_CHAR_RACE).
	        onParams(id, id_race).executeUpdate() > 0
	      case _ => SQL(eoceneSqlStrings.UPATE_CHAR_RACE).
	        onParams(id_race, id).executeUpdate() > 0
    	}
    }
  }

  /**
   * Get the Race of a character
   *
   * @param id the id of the character
   * @param c a sql connection. comes from the controller
   * @return Succes indicator
   */
  def getRaceIdByCharId(id: Int) = {
    DB.withConnection("chars") { implicit c =>
      val querry = SQL(eoceneSqlStrings.GET_CHAR_RACE).onParams(id)()
      querry.headOption match {
        case None => None
        case Some(row) => row[Option[Int]]("id_race")
      }
    }
  }

  /**
   * Update an attrribute using Purchase points
   *
   * @param id the id of the character
   * @param attribute the name of the atrribute (eg. "dex_mod")
   * @param direction either "up" or "down"
   * @param direction either "up" or "down"
   * @param c A sql connection. comes from the controller
   * @return Succes indicator
   */
  def updateCharAttributeWithPP(id: Int, attribute: String, direction: String): Boolean = {
    val current_value = getCharAttribute(id, attribute)
    Logger.info("current_value:%s".format(current_value))
    val pp = getCharAttribute(id, "pp")
    if (direction.equals("up")) {
      current_value match {
        case None => false //if this is not None then the char and the atrribute exist!
        case Some(13) => false
        case Some(value) =>
          updateCharAttribute(id, attribute, value + 1)
          updateCharAttribute(id, "pp", pp.get +
            eoceneServices.utilities.getPPCost(value + 1) -
            eoceneServices.utilities.getPPCost(value))
      }
    } else {
      current_value match {
        case None => false
        case Some(-3) => false
        case Some(value) =>
          updateCharAttribute(id, attribute, value - 1)
          updateCharAttribute(id, "pp", pp.get +
            eoceneServices.utilities.getPPCost(value - 1) -
            eoceneServices.utilities.getPPCost(value))
      }
    }
  }

  def getCharAttribute(id: Int, attribute: String): Option[Int] = {
    DB.withConnection("chars") { implicit c =>
      val querry = SQL(eoceneSqlStrings.GET_CHAR_ATTRIBUTE).onParams(id)()
      querry.headOption match {
        case None => None
        case Some(row) => row[Option[Int]](attribute)
      }
    }
  }

  /**
    * Update an attrribute
    *
    * @param id the id of the character
    * @param attribute the name of the atrribute (eg. "dex_mod")
    * @param direction either "up" or "down"
    * @param c A sql connection. comes from the controller
    * @return Success indicator
    */
  def updateCharAttribute(id: Int, attribute: String, new_value: Int): Boolean = {
    DB.withConnection("chars") { implicit c =>
      val result = SQL(eoceneSqlStrings.UPDATE_CHAR_ATTRIBUTE.format(attribute)).
        onParams(new_value, id).executeUpdate()
      Logger.info("attr:%s,new:%s, result:%s".format(attribute, new_value, result))
      return result > 0
    }
  }

  /**
   * Update an attrribute using Legend points
   *
   * @param id the id of the character
   * @param attribute the name of the atrribute (eg. "dex_mod")
   * @param direction either "up" or "down"
   * @param c a sql connection. comes from the controller
   * @return Succes indicator
   */
  def updateCharAttributeWithLP(id: Int, attribute: String, direction: String): Boolean = {
    DB.withTransaction("chars"){implicit c=>
	    val current_value = getCharAttribute(id, attribute)
	    val lp = getCharAttribute(id, "lp_sp")
	    if (direction.equals("up")) {
	      current_value match {
	        case None => return false
	        case Some(5) => return false
	        case Some(value) =>
	          updateCharAttribute(id, attribute, value + 1)
	          updateCharAttribute(id, "lp_sp", lp.get +
	            eoceneServices.utilities.getAttributeIncreaseLPCost(value + 1))

	      }
	    } else {
	      current_value match {
	        case None => return false
	        case _ => current_value.get match {
	          case 0 => return false
	          case _ =>
	            updateCharAttribute(id, attribute, current_value.get - 1)
	            updateCharAttribute(id, "lp_sp", lp.get -
	              eoceneServices.utilities.
	              getAttributeIncreaseLPCost(current_value.get))
	        }
        }
      }
    }
  }

  /**
   * Improve the discipline of a charcter. in case the character does not yet
   * have the discipline, add it.
   *
   * @param id the id of the character
   * @param id_discipline
   * @return success
   */
  def improveCharDiscipline(id: Int, id_discipline: Int) = {
    val disciplines = getCharDisciplineRowsByCharId(id)
    val target_discipline = disciplines.filter(a => a(1) == id_discipline)
    DB.withConnection("chars"){implicit c=>
	    if (target_discipline.length > 0) {
	      val circle = target_discipline(0)(2)
	      SQL(eoceneSqlStrings.UPDATE_CHAR_DISCIPLINE).onParams(circle + 1, id,
	        id_discipline).executeUpdate() > 0
	    } else {
	      SQL(eoceneSqlStrings.INSERT_CHAR_DISCIPLINE).onParams(id, id_discipline, 1)
	        .executeUpdate() > 0
	    }
    }
  }

  /**
    * Get  A List representing the chars_disciplines table entries for this
    * character
    *
    * @param id the id of the character
    * @return A nestetd List of integers corresponding to id_char,id_discipline
    *         and circle
    */
  def getCharDisciplineRowsByCharId(id: Int): List[List[Int]] = {
    DB.withConnection("chars") { implicit c => {
      val querry = SQL(eoceneSqlStrings.GET_CHAR_DISCIPLINES).onParams(id)()
      return querry.map(a => List(a[Int]("id_char"), a[Int]("id_discipline"),
        a[Int]("circle"))).toList
    }
    }
  }

  /**
   * Corrrupt the discipline of a charcter. In case the character we ar in
   * circle 1 -> remove the discipline
   *
   * @param id the id of the character
   * @param id_discipline
   * @return success
   */
  def corruptCharDiscipline(id: Int, id_discipline: Int) = {
    {
      val disciplines = getCharDisciplineRowsByCharId(id)
      val target_discipline = disciplines.filter(a => a(1) == id_discipline)
      target_discipline.headOption match {
        case None => false
        case _ =>
          val circle = target_discipline(0)(2)
          DB.withTransaction("chars"){implicit c=>
	          if (circle > 1) {
	            SQL(eoceneSqlStrings.UPDATE_CHAR_DISCIPLINE).onParams(circle - 1,
	              id, id_discipline).executeUpdate() > 0
	          } else {
	            SQL(eoceneSqlStrings.REMOVE_CHAR_DISCIPLINE)
	              .onParams(id, id_discipline)
	              .executeUpdate() > 0
	          }
          }
      }
    }
  }

  /**
   * Improve a talent (or learn it)
   *
   * @param id the id of the character
   * @param id_talent
   * @return success
   */
  def improveCharTalent(id: Int, id_talent: Int): Boolean = {
    val talents = getCharTalentRowsIdByCharId(id)
    val target_talent = talents.filter(a => a(1) == id_talent)
    val circle = getTalentCircleByTalenAndDisciId(id_talent, id)
    DB.withTransaction("chars"){implicit c=>
	    if (target_talent.length > 0) {
	      val step = target_talent(0)(2)
	      if (step == 15) return false
	      val lp_cost = eoceneServices.utilities.getTalentCost(step + 1, circle)
	      SQL(eoceneSqlStrings.UPDATE_CHAR_TALENT).onParams(step + 1, id,
	        id_talent).executeUpdate() > 0
	      SQL(eoceneSqlStrings.ADD_TO_LP).onParams(lp_cost, id).executeUpdate() > 0
	    } else {
	      val lp_cost = eoceneServices.utilities.getTalentCost(1, circle)
	      SQL(eoceneSqlStrings.INSERT_CHAR_TALENT).onParams(id, id_talent, 1)
	        .executeUpdate() > 0
	      SQL(eoceneSqlStrings.ADD_TO_LP).onParams(lp_cost, id)
	        .executeUpdate() > 0
	    }
    }
  }

  /**
   * Remove one circle (or forget)
   *
   * @param id the id of the character
   * @param id_talent
   * @return success
   */
  def corruptCharTalent(id: Int, id_talent: Int): Boolean = {
    val talents = getCharTalentRowsIdByCharId(id)
    val target_talent = talents.filter(a => a(1) == id_talent)
    if (target_talent.size == 0) return false
    val circle = getTalentCircleByTalenAndDisciId(id_talent, id)
    val step = target_talent(0)(2)
    val lp_cost = eoceneServices.utilities.getTalentCost(step, circle)
    DB.withTransaction("chars"){implicit c=>
	    if (step > 1) {
	      SQL(eoceneSqlStrings.UPDATE_CHAR_TALENT).onParams(step - 1, id,
	        id_talent).executeUpdate() > 0
	      SQL(eoceneSqlStrings.ADD_TO_LP).onParams(-lp_cost, id)
	        .executeUpdate() > 0
	    } else {
	      SQL(eoceneSqlStrings.REMOVE_CHAR_TALENT).onParams(id, id_talent)
	        .executeUpdate() > 0
	      SQL(eoceneSqlStrings.ADD_TO_LP).onParams(-lp_cost, id)
	        .executeUpdate() > 0
	    }
    }
  }

  /**
    * Get data for talents of a charcter
    *
    * @param id the id of the character
    * @param id_discipline
    * @return a nested list of integers with id_char, id_talent and step
    *         respectively
    */
  def getCharTalentRowsIdByCharId(id: Int): List[List[Int]] = {
    DB.withConnection("chars") { implicit c =>
      {
        val querry = SQL(eoceneSqlStrings.GET_CHAR_TALENTS).onParams(id)()
        return querry.map(a => List(a[Int]("id_char"), a[Int]("id_talent"),
          a[Int]("step"))).toList
      }
    }
  }

  /**
    * Get data for talents of a character while ensuring that the circle is the
    * lowest
    *
    * @param id the id of the character
    * @param id_discipline
    * @return success
    */
  def getTalentCircleByTalenAndDisciId(id_talent: Int, id_char: Int) = {
    DB.withConnection("chars") { implicit c =>
      val querry = SQL(eoceneSqlStrings.GET_TALENT_CIRCLE).onParams(
        id_char, id_talent)()
      querry(0)[Int]("circle")
    }
  }

  /**
   * Improve a skill (or learn it)
   *
   * @param id the id of the character
   * @param id_skill
   * @return success
   */
  def improveCharSkill(id: Int, id_skill: Int): Boolean = {
    val skills = getCharSkillRowsByCharId(id)
    val target_skill = skills.filter(a => a(1) == id_skill)
    DB.withTransaction("chars"){implicit c=>
	    if (target_skill.length > 0) {
	      val step = target_skill(0)(2)
	      if (step == 15) return false
	      SQL(eoceneSqlStrings.UPDATE_CHAR_SKILL).onParams(step + 1, id,
	        id_skill).executeUpdate() > 0
	      SQL(eoceneSqlStrings.ADD_TO_LP).onParams(
	        utilities.getSkillLPCcost(step + 1), id).executeUpdate() > 0
	    } else {
	      SQL(eoceneSqlStrings.INSERT_CHAR_SKILL).onParams(id, id_skill, 1)
	        .executeUpdate() > 0
	      SQL(eoceneSqlStrings.ADD_TO_LP).onParams(
	        utilities.getSkillLPCcost(1), id).executeUpdate() > 0
	    }
    }
  }

  def getCharSkillRowsByCharId(id: Int): List[List[Int]] = {
    DB.withConnection("chars") { implicit c => {
      val querry = SQL(eoceneSqlStrings.GET_CHAR_SKILLS).onParams(id)()
      return querry.map(a => List(a[Int]("id_char"), a[Int]("id_skill"),
        a[Int]("step"))).toList
    }
    }
  }

  /**
   * Corrupt a skill (or forget it)
   *
   * @param id the id of the character
   * @param id_skill
   * @return success
   */
  def corruptCharSkill(id: Int, id_skill: Int): Boolean = {
      val skills = getCharSkillRowsByCharId(id)
      val target_skill = skills.filter(a => a(1) == id_skill)
      if (target_skill.size == 0) return false
      val step = target_skill(0)(2)
      DB.withTransaction("chars"){implicit c=>
	      if (step > 1) {
	        SQL(eoceneSqlStrings.UPDATE_CHAR_SKILL).onParams(step - 1, id,
	          id_skill).executeUpdate() > 0
	        SQL(eoceneSqlStrings.ADD_TO_LP).onParams(
	          -utilities.getSkillLPCcost(step), id).executeUpdate() > 0
	      } else {
	        SQL(eoceneSqlStrings.REMOVE_CHAR_SKILL).onParams(id, id_skill)
	          .executeUpdate() > 0
	        SQL(eoceneSqlStrings.ADD_TO_LP).onParams(
	          -utilities.getSkillLPCcost(1), id).executeUpdate() > 0
	      }
      }
    }

  /**
   * Return all Spells of a character
   *
   * @param id the id of the character
   * @return A nested list
   */
  def getCharSpellListByCharId(id: Int): List[List[Int]] = {
    DB.withConnection("chars") { implicit c =>
      {
        val querry = SQL(eoceneSqlStrings.GET_CHAR_SPELLS).onParams(id)()
        return querry.map(a => List(a[Int]("id_char"), a[Int]("id_spell"),
          a[Int]("step"))).toList
      }
    }
  }

  /**
   * Learn a spell
   *
   * @param id id of the character
   * @param id_spell
   * @return success
   */
  def learnCharSpell(id: Int, id_Spell: Int) = {
    DB.withConnection("chars"){implicit c=>
	    SQL(eoceneSqlStrings.INSERT_CHAR_SPELL).onParams(id,
	      id_Spell, None).executeUpdate() > 0
    }
  }

  /**
   * Forget a spell
   *
   * @param id id of the character
   * @param id_spell
   * @return success
   */
  def unlearnCharSpell(id: Int, id_Spell: Int) = {
    DB.withConnection("chars"){implicit c=>
    	SQL(eoceneSqlStrings.REMOVE_CHAR_SPELL).onParams(id, id_Spell, None)
    	.executeUpdate() > 0
    }
  }

  /**
   * Change the anme of the character
   *
   * @param id id of the character
   * @param name teh new name
   * @return success
   */
  def changeCharName(id: Int, name: String) = {
    DB.withConnection("chars"){implicit c=>
    	SQL(eoceneSqlStrings.UPATE_CHAR_NAME).onParams(name, id)
      .executeUpdate() > 0
    }
  }

  /**
   * Add armor
   *
   * @param id id of the character
   * @param id_armor
   * @return success
   */
  def getArmor(id: Int, id_armor: Int) = {
    DB.withConnection("chars"){implicit c=>
    	eoceneSqlStrings.INSERT_ARMOR.onParams(id, id_armor).executeUpdate() > 0
    }
  }

  /**
   * Remove
   *
   * @param id id of the character
   * @param id_armor
   * @return success
   */
  def removeArmor(id: Int, id_armor: Int) = {
    DB.withConnection("chars"){implicit c=>
    	eoceneSqlStrings.REMOVE_ARMOR.onParams(id, id_armor).executeUpdate() > 0
    }
  }

  /**
   * Attach a threat to an armor
   *
   * @param id id of the character
   * @param id_armor
   * @return success
   */
  def attachThreadArmor(id: Int, id_armor: Int) = {
    DB.withConnection("chars"){implicit c=>
    	eoceneSqlStrings.UPATE_ARMOR.onParams(1, id, id_armor).executeUpdate() > 0
    }
  }

  /**
   * Remove a threat from an armor
   *
   * @param id id of the character
   * @param id_armor
   * @return success
   */
  def removeThreadArmor(id: Int, id_armor: Int) = {
    DB.withConnection("chars"){implicit c=>
    	eoceneSqlStrings.UPATE_ARMOR.onParams(-1, id, id_armor).executeUpdate() > 0
    }
  }

  /**
   * Add a spell to a matrix
   *
   * @param id_spell id of the spell
   * @param id_char id of the character
   * @return success
   */
  def Spell2Matrix(id_spell: Int, id_char: Int) = {
    DB.withConnection("chars") { implicit c =>
      eoceneSqlStrings.ADD_SPELL_2_MATRIX.onParams(id_spell, id_char)
        .executeUpdate() > 0
    }
  }

  /**
   * Remove a spell from a matrix
   *
   * @param id_spell id of the spell
   * @param id_char id of the character
   * @return success
   */
  def SpellFromMatrix(id_spell: Int, id_char: Int) = {
    DB.withConnection("chars") { implicit c =>
      eoceneSqlStrings.REMOVE_SPELL_FROM_MATRIX.onParams(id_spell, id_char)
        .executeUpdate() > 0
    }
  }

  /**
   * Remove the current user from the users allowed to edit a character
   *
   * @param id_char
   * @return Redirect
   */
  def removeUserFromChar(id_char: Int, id_user: String) = {
    DB.withConnection("chars") { implicit c =>
      eoceneSqlStrings.REMOVE_USER_FROM_CHAR.onParams(id_char, id_user)
        .executeUpdate > 0
    }
  }

  /**
   * Share a character with a user
   *
   * @param id_char
   * @return Redirect
   */
  def shareChar(id_char: Int, user_mail: String) = {
    DB.withConnection("chars") { implicit c =>
      try {
        val result = eoceneSqlStrings.INSERT_CHARS_USERS_BY_MAIL.
          onParams(id_char, user_mail)
          .executeUpdate > 0
        eoceneUserService.updateUsersChars
        result
      } catch {
        case e: MySQLIntegrityConstraintViolationException => false
        case e: Throwable => {
          Logger.error("""Error when adding user to character.
        					 Error was: "+e""")
          false
        }
      }
    }
  }

  /**
   * Buy Karma with LP
   *
   * @param id_char
   * @param nr_points the number of karma points to buy
   * @return Redirect
   */
  def buyKarma(id_char: Int, nr_points: Int) = {
    DB.withTransaction("chars"){implicit c=>
    val char = getCharById(id_char)
	    SQL(eoceneSqlStrings.UPDATE_CHAR_ATTRIBUTE.format("kar_curr"))
        .onParams(char.get.karCurr + nr_points, id_char).executeUpdate > 0 &&
	      SQL(eoceneSqlStrings.UPDATE_CHAR_ATTRIBUTE.format("lp_sp"))
          .onParams(char.get.lpSp + nr_points * char.get.race.kar_cost, id_char)
	      .executeUpdate > 0
    }
  }

  /**
   * Spent Karma points
   *
   * @param id_char
   * @param nr_points the number of karma points spent
   * @return Redirect
   */
  def spentKarma(id_char: Int, nr_points: Int) = {
    DB.withConnection("chars"){implicit c=>
	    val char = getCharById(id_char)
	    SQL(eoceneSqlStrings.UPDATE_CHAR_ATTRIBUTE.format("kar_curr"))
        .onParams(char.get.karCurr - nr_points, id_char).executeUpdate > 0
    }
  }

  /**
    * Get the id of a character with a certain name
    *
    * @param id
    * @return Creates the character of that id from the database
    */
  def getCharById(id: Int) = {
    DB.withTransaction("chars") { implicit c =>
      val char_querry = eoceneSqlStrings.GET_CHAR_BY_ID.onParams(id)()
      char_querry.size match {
        case 0 => None
        case _ =>
          val race_querry = eoceneSqlStrings.RACE_JOIN.onParams(id)()
          val disciplines_querry = eoceneSqlStrings.DISCIPLINE_JOIN.onParams(id)().
            groupBy(row => row[Int]("Disciplines.id"))
          val talent_querry = eoceneSqlStrings.TALENT_JOIN.onParams(id)()
          val spell_querry = eoceneSqlStrings.SPELL_JOIN.onParams(id)()
          val skill_querry = eoceneSqlStrings.SKILL_JOIN.onParams(id)()
          val armor_map = eoceneSqlStrings.GET_ARMOR.onParams(id)().
            groupBy(row => row[Int]("Armors.id"))
          val row = char_querry.head
          val race = getRaceWithRow(race_querry.head).get
          val disciplines = disciplines_querry.map(rows =>
            getDisciplineByRow(rows._2)).toList
          val talents = utilities.popDurability(talent_querry.map(
            row => getTalentByRow(row)).toList)
          val spells = spell_querry.map(row => getSpellByRow(row)).toList
          val skills = skill_querry.map(row => getSkillByRow(row)).toList
          val armors = armor_map.map(rows => getArmorByRows(rows._2)).toList
          val derived_values = getDerivedValues(row, race, talents, armors,
            disciplines)
          Some(Character(row[Int]("id"), row[String]("name"), row[Int]("dex_mod"),
            row[Int]("str_mod"), row[Int]("cha_mod"), row[Int]("tou_mod"),
            row[Int]("wil_mod"), row[Int]("per_mod"), row[Int]("dex_level"),
            row[Int]("str_level"), row[Int]("cha_level"), row[Int]("tou_level"),
            row[Int]("wil_level"), row[Int]("per_level"), row[Int]("lp_av"),
            row[Int]("lp_sp"), row[Int]("kar_curr"), row[Int]("pp"), race,
            disciplines, talents, spells, skills, armors, derived_values))
      }
    }
  }

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
    * Get a Armor
    * @param rows the rows from a dab call each row might correspond to a power
    * @return Armor
    */
  def getArmorByRows(rows: Stream[anorm.Row]) = {
    //var powers:List[Power] = List()
    val powers = rows(0)[Option[Int]]("thread") match {
      case None => List()
      case _ => rows.map(row => Power.getPowerByRow(row)).toList
    }
    val row = rows.head
    Armor(row[Int]("id"), row[String]("name"), row[Int]("cost"),
      row[Int]("init"), row[Int]("phys"), row[Int]("mystic"),
      row[Option[Int]]("shatter"), powers, row[Option[Int]]("threads"))
  }

  /**
    * Get a Race By id
    *
    * @param row
    * @return Some(Race)
    */
  def getRaceWithRow(row: anorm.Row): Option[Race] = {
    Some(Race(row[Int]("id"), row[String]("name"), row[Int]("dex_mod"),
      row[Int]("str_mod"), row[Int]("cha_mod"), row[Int]("tou_mod"),
      row[Int]("wil_mod"), row[Int]("per_mod"), row[Int]("k_step"),
      row[Int]("kar_start"), row[Int]("kar_max"), row[Int]("kar_cost"),
      row[Int]("movement"), row[String]("abilities"), row[Int]("social_def"),
      row[Int]("spell_def"), row[Int]("rec_test"), row[Int]("phys_arm"),
      row[Int]("wound_tresh"), row[Int]("phys_def")))
  }

  /**
    * Returns a Talent created from the entries in the row
    *
    * @param row: A row from a db querry
    * @return Talent
    */
  def getTalentByRow(row: anorm.Row) = {
    Talent(row[Int]("id"), row[String]("name"), row[Boolean]("action"),
      row[Boolean]("karma"), row[String]("strain"), row[String]("formula"),
      row[Option[Int]]("step"), row[Option[Boolean]]("disciplined"),
      row[Option[Int]]("talents_disciplines.circle"),
      row[Option[Int]]("talents_disciplines.id_discipline"))
  }

  /**
    * Get a Skill build from row
    *
    * @param rows the rows from a db call
    * @return Discipline
    */
  def getSkillByRow(row: anorm.Row) = {
    Skill(row[Int]("id"), row[String]("name"), row[String]("formula"),
      row[String]("type"), row[String]("comm"), row[Option[Int]]("step"))
  }

  /**
    * Calculate the Values that depend on the characteristic of a charactor.
    *
    * @param row A row representing the character
    * @param race the characters Race
    * @param talents the characters talents
    * @param armors the characters armors
    * @return a map of values
    *
    *         The methods should be moved to the class and only called after the Char
    *         object was created.
    */
  def getDerivedValues(row: anorm.Row, race: Race, talents: List[Talent],
                       armors: List[Armor], disciplines: List[Discipline]): Map[String, Any] = {
    val attributes = Map(
      "dex" -> (5 + row[Int]("dex_mod") + row[Int]("dex_level") + race.dex_mod),
      "str" -> (5 + row[Int]("str_mod") + row[Int]("str_level") + race.str_mod),
      "cha" -> (5 + row[Int]("cha_mod") + row[Int]("cha_level") + race.cha_mod),
      "tou" -> (5 + row[Int]("tou_mod") + row[Int]("tou_level") + race.tou_mod),
      "wil" -> (5 + row[Int]("wil_mod") + row[Int]("wil_level") + race.wil_mod),
      "per" -> (5 + row[Int]("per_mod") + row[Int]("per_level") + race.per_mod))
    Map(
      "dex" -> (5 + row[Int]("dex_mod") + row[Int]("dex_level") + race.dex_mod),
      "str" -> (5 + row[Int]("str_mod") + row[Int]("str_level") + race.str_mod),
      "cha" -> (5 + row[Int]("cha_mod") + row[Int]("cha_level") + race.cha_mod),
      "tou" -> (5 + row[Int]("tou_mod") + row[Int]("tou_level") + race.tou_mod),
      "wil" -> (5 + row[Int]("wil_mod") + row[Int]("wil_level") + race.wil_mod),
      "per" -> (5 + row[Int]("per_mod") + row[Int]("per_level") + race.per_mod),
      "dex_step" -> eoceneServices.utilities.getAttrStep(attributes("dex")),
      "str_step" -> eoceneServices.utilities.getAttrStep(attributes("str")),
      "cha_step" -> eoceneServices.utilities.getAttrStep(attributes("cha")),
      "tou_step" -> eoceneServices.utilities.getAttrStep(attributes("tou")),
      "wil_step" -> eoceneServices.utilities.getAttrStep(attributes("wil")),
      "per_step" -> eoceneServices.utilities.getAttrStep(attributes("per")),
      "physDef" -> (eoceneServices.utilities.getAttrDefense(attributes("dex")) +
        disciplines.map(discipline =>
          discipline.getModifierValueByName("physDef"))
          .foldLeft(0)((a1, a2) => a1 + a2) +
        race.phys_def),
      "spellDef" -> (eoceneServices.utilities.getAttrDefense(attributes("per")) +
        race.spell_def +
        disciplines.map(discipline =>
          discipline.getModifierValueByName("spellDef"))
          .foldLeft(0)((a1, a2) => a1 + a2)),
      "socDef" -> (eoceneServices.utilities.getAttrDefense(attributes("cha")) +
        race.social_def + disciplines.map(discipline =>
        discipline.getModifierValueByName("socDef"))
        .foldLeft(0)((a1, a2) => a1 + a2)),
      "movement" -> eoceneServices.utilities.getAttrMovement(attributes("dex") +
        race.movement),
      "carrying" -> eoceneServices.utilities.getAttrCarrying(attributes("str")),
      "death" -> (eoceneServices.utilities.getAttrDeath(attributes("tou")) +
        utilities.getDurability(talents)(0)),
      "unconc" -> (eoceneServices.utilities.getAttrUnconc(attributes("tou")) +
        utilities.getDurability(talents)(1)),
      "wound" -> eoceneServices.utilities.getAttrWound(attributes("tou")),
      "rec" -> (eoceneServices.utilities.getAttrRec(attributes("tou")) +
        race.rec_test +
        disciplines.map(discipline =>
          discipline.getModifierValueByName("rec"))
          .foldLeft(0)((a1, a2) => a1 + a2)),
      "mystic" -> (eoceneServices.utilities.getAttrMystic(attributes("wil")) +
        race.phys_armor +
        armors.map(armor => armor.getMysticalArmor()).
          foldLeft(0)((a1, a2) => a1 + a2)),
      "physical" -> armors.map(armor => armor.getPhysicalArmor()).
        foldLeft(0)((a1, a2) => a1 + a2),
      "initiative" -> (eoceneServices.utilities.getAttrStep(attributes("dex")) +
        armors.map(armor => armor.getInitiativeBonus()).
          foldLeft(0)((a1, a2) => a1 + a2) +
        disciplines.map(discipline =>
          discipline.getModifierValueByName("ini"))
          .foldLeft(0)((a1, a2) => a1 + a2)))
  }

  /**
    * Get a Discipline
    *
    * @param rows the rows from a db call each row might correspond to a modifier
    * @return Discipline
    */
  def getDisciplineByRow(discipline_rows: Stream[anorm.Row]) = {
    val modifiers = discipline_rows(0)[Option[Int]]("disciplines_modifiers.circle") match {
      case None => List()
      case _ => discipline_rows.map(row => Modifier.getModifierByRow(row))
        .toList
    }
    val row = discipline_rows.head
    Discipline(row[Int]("id"), row[String]("name"), row[String]("abilities"),
      row[Option[Int]]("chars_disciplines.circle"), modifiers)
  }

  /**
   * Adding legend points
   *
   * @param id_char
   * @param nr_points the number of lp to add
   * @return Redirect
   */
  def addLP(id_char: Int, nr_points: Int) = {
    DB.withConnection("chars"){implicit c=>
	    val char = getCharById(id_char)
	    SQL(eoceneSqlStrings.UPDATE_CHAR_ATTRIBUTE.format("lp_av"))
        .onParams(char.get.lpAv + nr_points, id_char).executeUpdate > 0
    }
  }
  
  def storeAction(call: String, id_char: Int, id_user: String) = {
    DB.withConnection("chars"){implicit c =>
    	eoceneSqlStrings.INSERT_HISTORY.onParams(call, id_char, id_user).executeUpdate() > 0
    }
  }

  def getCharsForUser(userid:String): List[(String, Int)] = {
      DB.withConnection("chars") { implicit c =>
      	val result = eoceneSqlStrings.GET_CHARS_FOR_USER.onParams(
        userid)().map(row => (row[String]("name"), row[Int]("id"))).toList
        return result
      }
     }

  def getDisciplines() = {
    DB.withConnection("chars") { implicit c =>
	    eoceneSqlStrings.GET_DISCIPLINES().groupBy(row => row[Int]("Disciplines.id")).
	      map(row => getDisciplineByRow(row._2)).toList.
	      sortWith((a1, a2) => a1.name < a2.name)
    }
  }

  def getSkills() = {
    DB.withConnection("chars") { implicit c =>
    	eoceneSqlStrings.GET_SKILLS().map(row => getSkillByRow(row)).toList
    }
  }

  def getSpells() ={
    DB.withConnection("chars") { implicit c =>
    eoceneSqlStrings.GET_SPELLS().map(row => getSpellWithDisciplineAsMap(row)).toList
    }
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

  def getSpellsForChar(id: Int) = { 
    DB.withConnection("chars") { implicit c =>
	    eoceneSqlStrings.GET_SPELLS_FOR_CHAR.onParams(id)().map(
	      row => getSpellWithDisciplineAsMap(row)).toList
    }
  }

  /**
   * Return a list of Availiable Armor Objects
   *
   * @param
   * @return a list of Armor
   */
  def getArmors() = {
    DB.withConnection("chars") { implicit c =>
	    eoceneSqlStrings.GET_ALL_ARMORS().groupBy(x => x[Int]("armors.id"))
	      .map(rows => getArmorByRows(rows._2)).toList.sortBy(x => x.id).reverse
    }
  }
}