/*
 * Copyright (c) 2016 Christian Garbers.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Simplified BSD License
 *  which accompanies this distribution
 *  Contributors:
 *       Christian Garbers - initial API and implementation
 */
package eoceneServices
import anorm._

object eoceneSqlStrings {

  val INSERT_CHAR = """
	    INSERT INTO Chars 
	  	VALUES (NULL, {name}, 5, 5, 5, 5, 5, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30)
	    """

  val GET_CHAR_ID_FROM_NAME = """
	    SELECT id FROM Chars 
	  	WHERE name={name}
	    """
  val GET_CHAR_BY_ID = SQL("""
      SELECT * FROM Chars 
      WHERE id = {name}
      """)

  val GET_CHAR_ATTRIBUTE = """
	    SELECT * FROM Chars WHERE id={id}
	    """

  val UPDATE_CHAR_ATTRIBUTE = """
	    UPDATE Chars
      	SET %s={id_race}
      	WHERE id={char_id};
	    """
  val UPDATE_CHAR_ATTRIBUTE_AND_PP = """
    	START TRANSACTION;
      	UPDATE Chars SET %s={new_mod} WHERE id={char_id};
    	UPDATE Chars SET pp={new_pp} WHERE id={char_id};
    	COMMIT;
      """

  val UPATE_CHAR_RACE = """
	    UPDATE chars_races
      	SET id_race={id_race}
      	WHERE id_char={id_char};
	    """

  val SET_CHAR_RACE = """
	    INSERT INTO chars_races
    	VALUES ({id_char},{id_race})
	    """

  val GET_CHAR_RACE = """
	    SELECT id_race FROM chars_races WHERE id_char={id_char}
	    """

  val GET_CHAR_DISCIPLINES = """
	    SELECT * FROM chars_disciplines WHERE id_char={id}
	    """

  val UPDATE_CHAR_DISCIPLINE = """
	    UPDATE chars_disciplines
      	SET circle = {circle}
      	WHERE id_char={char_id} AND id_discipline={id_disc};
	    """

  val INSERT_CHAR_DISCIPLINE = """
	    INSERT INTO chars_disciplines
    	VALUES ({a},{b},{c})
	    """

  val REMOVE_CHAR_DISCIPLINE = """
	    DELETE FROM chars_disciplines
    	WHERE id_char={id} AND id_discipline={id_disc}
	    """

  val GET_CHAR_TALENTS = """
	    SELECT * FROM chars_talents WHERE id_char={id}
	    """

  val UPDATE_CHAR_TALENT = """
	    UPDATE chars_talents
      	SET step = {step}
      	WHERE id_char={id_char} AND id_talent={id_talent};
	    """

  val INSERT_CHAR_TALENT = """
	    INSERT INTO chars_talents
    	VALUES ({a},{b},{c})
	    """

  val REMOVE_CHAR_TALENT = """
	    DELETE FROM chars_talents
    	WHERE id_char={id_cahr} AND id_talent={id_talent}
	    """

  val GET_CHAR_SKILLS = """
	    SELECT * FROM chars_skills WHERE id_char={id}
	    """

  val UPDATE_CHAR_SKILL = """
	    UPDATE chars_skills
      	SET step = {step}
      	WHERE id_char={char_id} AND id_skill={ckill_id};
	    """

  val INSERT_CHAR_SKILL = """
	    INSERT INTO chars_skills
    	VALUES ({a},{b},{c})
	    """

  val REMOVE_CHAR_SKILL = """
	    DELETE FROM chars_skills
    	WHERE id_char={id_char} AND id_skill={id_skill}
	    """

  val GET_CHAR_SPELLS = """
	    SELECT * FROM chars_spells WHERE id_char={id}
	    """

  val INSERT_CHAR_SPELL = """
	    INSERT INTO chars_spells
    	VALUES ({a},{b},{C})
	    """

  val REMOVE_CHAR_SPELL = """
	    DELETE FROM chars_spells
    	WHERE id_char={id_char} AND id_spell={id_spell}
	    """

  val GET_RACE_BY_ID = """
      SELECT * FROM Races 
      WHERE id={id}
      """

  val GET_TALENT_BY_ID = """
      SELECT * FROM Talents
      WHERE id={id}
      """

  val GET_DISCIPLINE_BY_ID = """
      SELECT * FROM Disciplines
      WHERE id={id}
      """
  val GET_SPELL_BY_ID = """
      SELECT * FROM Spells
      WHERE id={id}
      """
  val GET_SKILL_BY_ID = """
      SELECT * FROM Skills
      WHERE id={id}
      """

  val GET_TALENT_ID_BY_DISCIPLINE_ID = """
      SELECT * FROM talents_disciplines
      WHERE id_discipline = {id} AND circle<={id}
      """

  val TALENT_JOIN = SQL("""
      SELECT * FROM Talents 
      JOIN 
      talents_disciplines 
      ON Talents.id=talents_disciplines.id_talent 
      JOIN 
      chars_disciplines
      ON chars_disciplines.id_char={id} AND chars_disciplines.id_discipline=talents_disciplines.id_discipline AND chars_disciplines.circle>=talents_disciplines.circle
      LEFT JOIN
      chars_talents 
      ON chars_talents.id_char=chars_disciplines.id_char AND chars_talents.id_talent=Talents.id
      GROUP BY Talents.id ORDER BY chars_disciplines.id_discipline,chars_disciplines.circle, talents_disciplines.circle,Talents.id
      """)

  val SPELL_JOIN = SQL("""
	SELECT * FROM Spells
	JOIN chars_spells ON chars_spells.id_char = {id_char}
	AND chars_spells.id_spell = Spells.id JOIN
	chars_disciplines ON chars_disciplines.id_char=chars_spells.id_char JOIN
	disciplines_spells 
	ON disciplines_spells.id_discipline=chars_disciplines.id_discipline AND
	disciplines_spells.id_spell=Spells.id
	ORDER BY Spells.circle, Spells.name
      """)

  val SKILL_JOIN = SQL("""
      SELECT * FROM Skills
      JOIN 
      chars_skills ON chars_skills.id_char={id} AND chars_skills.id_skill=Skills.id   
      ORDER BY Skills.type,Skills.name
      """)

  val GET_SKILLS = SQL("""
      SELECT * FROM Skills
      LEFT JOIN 
      chars_skills ON 1=2
      ORDER BY Skills.type, Skills.name
      """)

  val DISCIPLINE_JOIN = SQL("""
		SELECT *
		FROM Disciplines
		JOIN chars_disciplines ON chars_disciplines.id_char = {id}
		AND chars_disciplines.id_discipline = Disciplines.id LEFT JOIN
		disciplines_modifiers ON 
		disciplines_modifiers.id_discipline =  Disciplines.id AND
		chars_disciplines.circle>=disciplines_modifiers.circle
		""")

  val RACE_JOIN = SQL("""
	    SELECT *
	    FROM Races
	    JOIN chars_races ON chars_races.id_char = {id}
	    AND chars_races.id_race = Races.id
	      """)

  val UPATE_CHAR_NAME = """
	    UPDATE Chars
      	SET name={name}
      	WHERE id={id_char};
	    """

  val GET_TALENT_CIRCLE = """
      	SELECT MIN(talents_disciplines.circle) AS circle  FROM 
        chars_disciplines JOIN talents_disciplines ON  
      	chars_disciplines.id_char={char_id} AND 
        chars_disciplines.id_discipline=talents_disciplines.id_discipline AND 
        talents_disciplines.id_talent={talent_id}
      """

  val ADD_TO_LP = """
      UPDATE Chars
      SET lp_sp = lp_sp+{lp}
      WHERE id={char_id}
      """

  val GET_RACES = SQL("""
      SELECT * FROM Races ORder BY Races.name
      """)

  val GET_DISCIPLINES = SQL("""
      SELECT * FROM Disciplines LEFT JOIN chars_disciplines ON 1=2 LEFT JOIN
      disciplines_modifiers ON 1=2 
      ORDER BY Disciplines.name 
      """)

  val GET_Skills = SQL("""
      SELECT * FROM Skills
      """)

  val GET_SPELLS = SQL("""
      SELECT * FROM Spells JOIN 
       disciplines_spells
       ON disciplines_spells.id_spell=Spells.id
       JOIN Disciplines 
       ON disciplines_spells.id_discipline=Disciplines.id
      """)

  val GET_SPELLS_FOR_CHAR = SQL("""
		SELECT * FROM Spells JOIN 
		disciplines_spells
		ON disciplines_spells.id_spell=Spells.id
		JOIN Disciplines 
		ON disciplines_spells.id_discipline=Disciplines.id JOIN
		chars_disciplines ON chars_disciplines.id_char={id_char} AND
		disciplines_spells.id_discipline = chars_disciplines.id_discipline
		ORDER BY Disciplines.id,Spells.circle
      """)

  val GET_ARMOR = SQL("""
		SELECT * FROM Armors JOIN chars_armors
		ON chars_armors.id_char = {id_char} AND chars_armors.id_armor=Armors.id LEFT JOIN Powers ON
		Powers.id_armor=Armors.id AND chars_armors.threads>=Powers.thread
      """)

  val GET_ALL_ARMORS = SQL("""
		SELECT *
		FROM Armors
		LEFT JOIN Powers ON Powers.id_armor = Armors.id
		LEFT JOIN chars_armors ON 1 =2
		ORDER BY Powers.id_armor
      """)

  val GET_POWR_BY_ARMOR_ID = SQL("""
  	  SELECT * FROM Powers 
      WHERE id_armor={id_armor}
      """)

  val GET_ARMOR_BY_ID = SQL("""
  	  SELECT * FROM Armors 
      WHERE id={id_armor}
      """)

  val INSERT_ARMOR = SQL("""
	    INSERT INTO chars_armors
    	VALUES ({a},{b},0)
	    """)

  val REMOVE_ARMOR = SQL("""
	    DELETE FROM chars_armors
    	WHERE id_char={id_char} AND id_armor={id_armor}
	    """)

  val UPATE_ARMOR = SQL("""
	    UPDATE chars_armors
      	SET threads=threads+{value}
      	WHERE id_char={id_char} AND id_armor={id_armor};
	    """)

  val INSERT_CHARS_USERS = SQL("""
	    INSERT INTO chars_users
    	VALUES ({char_id},{user_id})
	    """)

  val GET_CHARS_FOR_USER = SQL("""
	    SELECT * FROM Chars JOIN chars_users ON 
	    chars_users.id_char=Chars.id AND chars_users.id_user={id_user}  
	    """)

  val ADD_SPELL_2_MATRIX = SQL("""
		UPDATE chars_spells
      	SET spell_matrix=1
      	WHERE id_spell={id_spell} AND id_char={id_char};
	    """)

  val REMOVE_SPELL_FROM_MATRIX = SQL("""
		UPDATE chars_spells
      	SET spell_matrix=Null
      	WHERE id_spell={id_spell} AND id_char={id_char};
	    """)
  val REMOVE_USER_FROM_CHAR = SQL("""
	    DELETE FROM chars_users
    	WHERE id_char={id_char} AND id_user={id_user}
	    """)

  val INSERT_CHARS_USERS_BY_MAIL = SQL("""
		  INSERT INTO chars_users VALUES ({char_id},
		  (SELECT userId FROM Users WHERE email ={mail}))
	    """)

  val INSERT_HISTORY = SQL("""
		  INSERT INTO History VALUES ({call},{char_id},{user_id})
	    """)

}

