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
import play.api.mvc._
import play.api.db._
import play.api.Play.current
import anorm._
import play.api.db.DB
import java.sql.Connection
import models.Race
import models.Armor
import models.Talent
import scala.util.matching.Regex
import scala.util.Random

object utilities {

  val rand_gen = new Random()
  
   /**
   * a tabel modelling legend point costs when learning talents
   */  
  val TalenRankCost: List[List[Int]] = List(
    List(100, 200, 300, 500),
    List(200, 300, 500, 800),
    List(300, 500, 800, 1300),
    List(500, 800, 1300, 2100),
    List(800, 1300, 2100, 3400),
    List(1300, 2100, 3400, 5500),
    List(2100, 3400, 5500, 8900),
    List(3400, 5500, 8900, 14400),
    List(5500, 8900, 14400, 23300),
    List(8900, 14400, 23300, 37700),
    List(14400, 23300, 37700, 6100),
    List(23300, 37700, 61000, 98700),
    List(37700, 61000, 98700, 159700),
    List(61000, 98700, 159700, 258400),
    List(98700, 159700, 258400, 418100))

   /**
   * a map modeeling legend point costs when increasing attributes
   */  
  val AtrributeIncreaseCost = Map(1 -> 800, 2 -> 1300, 3 -> 2100, 4 -> 3400, 5 -> 5500)
   
  /**
   * a map modelling legend point costs when increasing skills
   */  
  val SkillIncreaseCost = Map(1 -> 200, 2 -> 300, 3 -> 500, 4 -> 800, 5 -> 1300, 6 -> 2100,
    7 -> 3400, 8 -> 5500, 9 -> 8900, 10 -> 14400)

   /**
   * a map providing the seed for step to dice conversion
   */  
  val DiceStep = Map(
		  			0->"0",
		  			1->"d4+-2",
		  			2->"d4+-1",
		  			3->"1d4",
		  			4->"1d6",
		  			5->"1d8",
		  			6->"1d10",
		  			7->"1d12",
		  			8->"1d6+1d6",
		  			9->"1d8+1d6",
		  			10->"1d10+1d6",
		  			11->"1d10+1d8",
		  			12->"1d10+1d10",
		  			13->"1d12+1d10"
		  )
   /**
   * get the step associated with a given attribute value
   *
   * @param attr the value of the atrribute
   * @return the step value
   */  		  
  def getAttrStep(attr: Int) = (attr - 1) / 3 + 2
   /**
   * get the sfense rating assoiated with a atrribute value
   *
   * @param attr the value of the attrribute
   * @return the defense rating
   */  		
  def getAttrDefense(attr: Int): Int = {
    val base = (attr - 4) / 7
    val modu = (attr - 4) % 7

    if (List(0, 1, 2) contains modu) return base * 3 + 4
    else if (List(3, 4) contains modu) return base * 3 + 5
    else return base * 3 + 6
  }
  
   /**
   * get the movement assoatiated with a given attribute value
   *
   * @param attr the value of the atrribute
   * @return the movement rating
   */  
  def getAttrMovement(attr: Int): Int = {
    if (attr < 7) return 5 + attr
    else if (attr < 21) return attr * 2
    else return attr % 20 * 3 + 40
  }
  
   /**
   * get the carrying capacity assoatiated with a given attribute value
   *
   * @param attr the value of the atrribute
   * @return the carrying rating
   */  
  def getAttrCarrying(attr: Int): Int = {
    if (attr < 7) return attr * 5
    else if (attr < 12) return (attr - 7) * 10 + 40
    else if (attr < 16) return (attr - 12) * 15 + 95
    else if (attr < 19) return (attr - 16) * 20 + 160
    else if (attr < 22) return (attr - 19) * 30 + 230
    else if (attr < 28) return (attr - 22) * 40 + 330
    else return (attr - 28) * 60 + 620
  }
  
   /**
   * get the death rating
   *
   * @param attr the value of the atrribute
   * @return the death rating
   */  
  def getAttrDeath(attr: Int) = (attr / 3) + 18 + attr
   
  /**
   * get the unconconc  rating
   *
   * @param attr the value of the atrribute
   * @return the unconc rating
   */  
  def getAttrUnconc(attr: Int) = (attr / 3) + 9 + attr
  
   /**
   * get the wound threshold
   *
   * @param attr the value of the atrribute
   * @return the wound threshold rating
   */  
  def getAttrWound(attr: Int): Int = {
    if (attr < 20) return (attr / 2) + 3
    else if (attr < 23) return 13
    else return (attr - 1) / 2 + 3
  }
  
   /**
   * get the # of recovery tests
   *
   * @param attr the value of the atrribute
   * @return the # rec test
   */  
  def getAttrRec(attr: Int): Double = {
    if (attr < 3) return 0.5
    else if (attr < 8) return 1
    else return (attr - 8) / 6 + 2
  }
  
   /**
   * get the mystic armor
   *
   * @param attr the value of the atrribute
   * @return mystic armor
   */  
  def getAttrMystic(attr: Int): Int = {
    if (attr < 11) return 0
    else return (attr - 11) / 3 + 1
  }
   
  /**
   * calculate purchase points cost
   *
   * @param attribute value
   * @return cost
   */  
  def getPPCost(value: Int) = {
    val prices = Map(2 -> (-3), 3 -> (-2), 4 -> (-1), 5 -> 0, 6 -> 1, 7 -> 2, 8 -> 3, 9 -> 4, 10 -> 5,
      11 -> 6, 12 -> 8, 13 -> 10, 14 -> 13, 15 -> 16, 16 -> 19, 17 -> 21, 18 -> 23)
    prices(value + 5)
  }
  
  /**
   * calculate legend points cost
   *
   * @param attribute value
   * @return cost
   */  
  def getAttributeIncreaseLPCost(value: Int) = AtrributeIncreaseCost(value)
  
  /**
   * calculate legend points cost
   *
   * @param attribute value
   * @return cost
   */  
  def getSkillLPCcost(rank: Int) = SkillIncreaseCost(rank)
  
  /**
   * calculate legend points cost
   *
   * @param rank the rank of the talent
   * @param the circle of the talent
   * @return cost
   */  
  def getTalentCost(rank: Int, circle: Int) = {
    val circle_bin = (circle - 1) / 4
    TalenRankCost(rank-1)(circle_bin)
  }

  def getRaces()(implicit c: Connection) =
    eoceneSqlStrings.GET_RACES().map(row => Race.getRaceByRow(row)).toList
    .sortWith((a1,a2) =>a1.name<a2.name)

  def getDisciplines()(implicit c: Connection) =
    eoceneSqlStrings.GET_DISCIPLINES().groupBy(row=>row[Int]("Disciplines.id")).
    map(row => models.Discipline.getDisciplineByRow(row._2)).toList.
    sortWith((a1,a2) =>a1.name<a2.name)

  def getSkills()(implicit c: Connection) =
    eoceneSqlStrings.GET_SKILLS().map(row => models.Skill.getSkillByRow(row)).toList

  def getSpells()(implicit c: Connection) =
    eoceneSqlStrings.GET_SPELLS().map(row => models.Spell.getSpellWithDisciplineAsMap(row)).toList

  def getSpellsForChar(id:Int)(implicit c: Connection) =
    eoceneSqlStrings.GET_SPELLS_FOR_CHAR.onParams(id)().map(
        row => models.Spell.getSpellWithDisciplineAsMap(row)).toList
  
   /**
   * Return a list of Availiable Armor Objects
   *
   * @param 
   * @return a list of Armor
   */
  def getArmors()(implicit c: Connection) =
    eoceneSqlStrings.GET_ALL_ARMORS().groupBy(x=>x[Int]("armors.id"))
    .map(rows=> Armor.getArmorByRows(rows._2)).toList.sortBy(x=>x.id).reverse
  	
    /**
   * filter the durabilitie talent and return its values as a list of 2 integers
   *
   * @param talents list of talents
   * @return the durability ratings
   */  
  def getDurability(talents: List[Talent]): List[Int] = {
    val pattern = "\\(.+\\)".r
    val durs = talents.filter(talent => talent.name contains "Durability")
      .map(talent => Seq((pattern findFirstIn talent.name).get, talent.step))
      .map(talent => List(talent(0).asInstanceOf[String].charAt(1).asDigit,
        talent(0).asInstanceOf[String].charAt(3).asDigit, talent(1)))
      .sortWith(_(0).asInstanceOf[Int] > _(0).asInstanceOf[Int])
    
	if (durs.length>0){	
	  val durability = durs.head
	  val talent = durability(2).asInstanceOf[Option[Int]]  
	  if (talent != None) {
	      List(durability(0).asInstanceOf[Int] * durability(2).asInstanceOf[Option[Int]].get,
	        durability(1).asInstanceOf[Int] * durability(2).asInstanceOf[Option[Int]].get)
	    } else {
	      List(0, 0)
	    }
	  }
   else{return List(0,0)}
  }
  
  /**
   * Scans through talents and removes all but the first durability talent it
   * finds.
   *
   * @param talents A List of Talents
   * @return a list of talents (with only the durability of the first discipline
   *  left)
   */
  def popDurability(talents: List[Talent]) = {
	var first= true
	talents.filter(talent=>
	  if(!(talent.name contains "Dura")){
		  true
	  } 
	  else if(first){
	    first=false
	    true
	  }
	  else{
	    false
	  }
	  )
	}
  
  /**
   * Return the Dice corresponding to the step number
   *
   * @param step The Step Number
   * @return An Option with dice string
   */
  def getDiceForStep(step:Int):Option[String] = {
    if (step<0) return None
    if(step<14) Some(DiceStep(step))
    else if (step<25) Some("1d20+" + DiceStep(step-11))
    else Some("%sd20+".format((step-14)/11) + "1d10+1d8+" + DiceStep((step-14)%11+3))
  }
  
  /**
  * Return the result of rolling a dice with the indicated number of sides
  * 
  * @param dice the sides of the dice
  * @return number rolled
  */   	 
  def rollDice(dice:Int) :Int = {
    if (!(dice>1)) return 0
    val result = rand_gen .nextInt(dice)+1
    if(result==dice) return result + rollDice(dice)
    else return result
  }
  
  /**
  * Return the result of rolling dices according to the dice string
  * 
  * 
  * @param dice_string the dice string
  * @return number rolled
  */   	  
  def rollDiceString(dice_string:String):Int ={
    val pattern_1 = """(\d+)d(\d+)""".r
    val pattern_2 = """^d(\d+)""".r
    val pattern_3 = """s(\d+)""".r
    val pattern_4 = """^(\d+)$""".r
    val pattern_5 = """^-(\d+)$""".r
    dice_string.split("\\+").map(instruct=> instruct match{
		      case pattern_1(times, sides) => 1.to(times.toInt).
		    		  						  map(x=>rollDice(sides.toInt))
		    		  						  .reduce((a1,a2)=>a1+a2)
		      case pattern_2(sides) => rollDice(sides.toInt)
		      case pattern_3(step) => rollDiceString(getDiceForStep(step.toInt)
		    		  				  .get)
		      case pattern_4(value) => value.toInt
		      case pattern_5(value) => -1*value.toInt
		      case default => {Logger.error("I couldnt make sense of a dice instruction: %s".format(default))
		                	  0
		                }		      
    		}
    	)
        .reduce((a1,a2)=>a1+a2)            
  }
  
  def getRandomName() = {
    "NoName%sPleaseChangeME".format(rand_gen.nextInt(100000))
  }
  
  def storeAction(call:String, id_char:Int,id_user:String)(implicit c:Connection) = {
    eoceneSqlStrings.INSERT_HISTORY .onParams(call,id_char, id_user).executeUpdate()>0
  }

}
