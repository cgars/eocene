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

case class Modifier(val id: Int, val circle:Int, val value:Int)

object Modifier {
	val modfierName2Id = Map("physDef"-> 1,
							"spellDef"->2,
							"karmaMax"->3,
							"ini"->4,
							"socDef"->5,
							"rec"->6
							) 
							
  /**
   * Get a Modifier
   * 
   * @param rows the rows from a db call each row might correspond to a modifier
   * @return Modifier
   */ 
	def getModifierByRow(row:anorm.Row) = {
	  Modifier(row[Int]("disciplines_modifiers.id_modifier"),
			   row[Int]("disciplines_modifiers.circle"),
			   row[Int]("value"))
  }
}
