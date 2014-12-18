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
import play.api._
/**
 * Discipline
 */
case class Discipline(val id: Int, val name: String, val abilities: String,
  val circle: Option[Int], val modifiers:List[Modifier]){
  
  /**
   * Get the sum of modification this discipline receives due to a modifier 
   * 
   * @param name -> of the modfier ("physDef", "spellDef", "karmaMax", "ini"
   * 								  "socDef", "rec"->6)
   *           
   * @return The summed modiefiers
   */  
  def getModifierValueByName(name:String):Int = {
	  modifiers.filter(mod=>mod.id  == Modifier.modfierName2Id(name))
	  .map(mod => mod.value)
	  .reduceOption((a1,a2)=>a1+a2).getOrElse(0)
  }  
}

object Discipline {
  
  /**
   * Get a Discipline
   * 
   * @param rows the rows from a db call each row might correspond to a modifier
   * @return Discipline
   */ 
  def getDisciplineByRow(discipline_rows:Stream[anorm.Row]) = {
    val modifiers = discipline_rows(0)[Option[Int]]("disciplines_modifiers.circle") 
    match{
      case None => List()
      case _ => discipline_rows.map(row=>Modifier.getModifierByRow(row))
      			.toList
    }    
    val row = discipline_rows.head
    Discipline(row[Int]("id"), row[String]("name"), row[String]("abilities"),
      row[Option[Int]]("chars_disciplines.circle"), modifiers)
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
      groupBy(row=>row[Int]("Disciplines.id"))
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

  implicit object DisciplineFormat extends Format[Discipline] {

    def reads(json: JsValue) = JsSuccess(getDisciplineById(1))

    def writes(discipline: Discipline) = discipline.circle match {
      case None => JsObject(Seq("id" -> JsNumber(discipline.id),
        "name" -> JsString(discipline.name),
        "Abilities" -> JsString(discipline.abilities),
        "circle" -> JsNumber(0)))

      case discipline.circle => JsObject(Seq("id" -> JsNumber(discipline.id),
        "name" -> JsString(discipline.name),
        "Abilities" -> JsString(discipline.abilities),
        "circle" -> JsNumber(discipline.circle.get)))

    }
  }
}
