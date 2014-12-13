/*******************************************************************************
 * Copyright (c) 2014 Christian Garbers.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Simplified BSD License
 * which accompanies this distribution
 * 
 * Contributors:
 *     Christian Garbers - initial API and implementation
 ******************************************************************************/
package eoceneServices

import play.api._

class Validator(char:models.Char) {
	var message:String = ""
	val circle_requiremnents = Map(2 -> (5,2,1),
								   3 -> (6,3,2),
								   4 -> (7,4,3),
								   5 -> (8,5,4),
								   6 -> (9,6,5),
								   7 -> (10,7,6),
								   8 -> (11,8,7),
								   9 -> (12,9,8),
								   10 -> (13,10,9),
								   11 -> (14,11,10),
								   12 -> (15,11,11),
								   13 -> (16,12,12),
								   14 -> (17,12,13),
								   15 -> (18,13,14)
								   )
	    
	def validate():Boolean = {
	 if (char.disciplines .size==0){
	   message += "No Discipline selcted"
	   return false
	 }
	return char.disciplines.map(discipline=>checkDiscilineCircleRequirements(discipline)).
	reduce((a1,a2)=>a1 && a2)&&
	checkAtrributeImprovements
	}
	
	def checkDiscilineCircleRequirements(discipline:models.Discipline):Boolean = {
	  if (discipline.circle.get==1)return true
	  ((2).to(discipline.circle.get)).
	  map(circle=>eligableForCircle(circle, discipline)).
	  reduce((a1,a2)=>a1 && a2)	  
	}
	
	def eligableForCircle(circle:Int, discipline:models.Discipline):Boolean = {
	  val talents_o_discipline = char.talents.
	  filter(talent => talent.step!=None ).
	  filter(talent => talent.discipline_id.get==discipline.id )
	  val nr_talents = talents_o_discipline.size>=circle_requiremnents(circle)._1 
	  val min_rank = talents_o_discipline.filter(talent=>talent.step.get>=circle)
	  .size>=circle_requiremnents(circle)._2
	  val single_talent = talents_o_discipline.
	  filter(talent=>talent.circle.get>circle-2).
	  filter(talent=>talent.step.get>=circle_requiremnents(circle)._3).size>0
	  if(!nr_talents){
	    message+="\nRequired: A minimum of %s %s talents is required to be %s in circle %s".
	    format(circle_requiremnents(circle)._1, discipline.name, discipline.name, circle )
	  }
	  if (!min_rank){
	    message+="\nRequired: A minimum of %s %s Talents of rank %s is required to be %s in circle %s".
	    format(circle_requiremnents(circle)._2, discipline.name, circle,discipline.name, circle)
	  }
	  if (!single_talent){
	    message+="\nRequired: One %s talents from circle %s with at least rank %s is required to be %s in circle %s".
	    format(discipline.name, circle-1, circle_requiremnents(circle)._3, discipline.name, circle)
	  }
	  nr_talents&&min_rank&&single_talent
	}
	
	def checkAtrributeImprovements():Boolean = {
	  var allowed = 0
	  if (!(char.disciplines.size==0)) {
	    allowed += char.disciplines.head.circle.get-1
	   char.disciplines.takeRight(char.disciplines .size-1)
		  .map(discipline=>allowed+=discipline.circle.get/2)
	  }
	  
	  val improvements = char.dex_level + char.cha_level +char.per_level +
			  			 char.will_level +char.tou_level +char.str_level
	  if(improvements>allowed){
	    message+="You have improved to many atrributes"
	    return false
	  }
	  else return true
	  
	}
}

object Validator {
  
  def getValidator(char:models.Char):eoceneServices.Validator = {
    val validator = new  Validator(char)    
    return validator
  }
  
}
