/**
 * *****************************************************************************
 * Copyright (c) 2014 Christian Garbers.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Simplified BSD License
 * which accompanies this distribution
 *
 * Contributors:
 *     Christian Garbers - initial API and implementation
 * ****************************************************************************
 */
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
import scala.math

object utilities {

  val rand_gen = new Random()
   
  /**
   * a Map with parameters to calculate cumulative densities
   * for a extrem value distribution
   */
  val CdfParamters = Map(
	 2->List(1.26707783771,-0.000254153519491,0.000322032291922),
	 3->List(-0.649204414684,1.7748560209,1.08244557495),
	 4->List(-0.308262613858,2.57443267042,1.72067032005),
	 5->List(-0.207132941854,3.3091170042,2.27553307393),
	 6->List(1.1328046695,5.27350381929,4.22139227439),
	 7->List(1.05793617733,6.36002691325,4.90879539031),
	 8->List(-0.130806964159,6.1920458635,3.07798321753),
	 9->List(-0.0899829876766,6.99209972337,3.47999167816),
	 10->List(-0.0654180853914,7.78338138804,3.91026577225),
	 11->List(-0.0413375976784,8.59614251506,4.24398660942),
	 12->List(-0.0272519402646,9.42450735466,4.63991122437),
	 13->List(-0.0127603041078,10.1318042667,4.97171794191),
	 14->List(0.00952466736669,10.9024994138,6.14936232258),
	 15->List(0.0155610884614,11.7093814667,6.3071881403),
	 16->List(0.0199633008529,12.5220015901,6.50290285494),
	 17->List(0.0303792073089,13.4414989541,6.83668067809),
	 18->List(0.0360929204432,14.2524235825,7.12084860626),
	 19->List(0.0320525633697,15.6601289469,7.01338830138),
	 20->List(0.0348879946341,16.5082843753,7.2298078663),
	 21->List(0.0467978899156,17.4020402347,7.47668990994),
	 22->List(0.0474975597631,18.2734388622,7.70323502467),
	 23->List(0.0505293243237,19.1189245561,7.90722222869),
	 24->List(0.0592504361995,20.0026932394,8.20910918262),
	 25->List(0.0587017786747,21.4125370044,8.12456359004),
	 26->List(0.057562330318,22.2586029406,8.28429176343),
	 27->List(0.0643098293539,23.1123562786,8.49270534666),
	 28->List(0.0665401370862,23.9428448541,8.63124720943),
	 29->List(0.0699904325025,24.8284793575,8.91670556105),
	 30->List(0.0705594334206,26.2267666953,8.81314678709),
	 31->List(0.0699282579213,27.0475790983,9.00328278947),
	 32->List(0.0783656769118,27.9748202933,9.21490931875),
	 33->List(0.0798724051728,28.8347109745,9.35892611044),
	 34->List(0.0795109248625,29.7172490885,9.57386426448),
	 35->List(0.0834580629747,30.6191691229,9.82596400817),
	 36->List(0.0876586858268,31.5381127494,10.4938729435),
	 37->List(0.0938118667111,32.3738926444,10.6617376554),
	 38->List(0.0960907479211,33.2667987508,10.7460291442),
	 39->List(0.0903008272762,34.1157878043,10.9182367937),
	 40->List(0.0955858261346,34.9769455545,11.1692401805),
	 41->List(0.0993087237618,36.389932986,11.0121600519),
	 42->List(0.0983655199432,37.3054890073,11.2002097607),
	 43->List(0.0968547276717,38.178010705,11.364195093),
	 44->List(0.098005227154,39.1130203947,11.5282662184),
	 45->List(0.102856429625,39.9666622192,11.6802726495),
	 46->List(0.101562723294,40.8696202607,11.8382471457),
	 47->List(0.103901752155,41.8113035856,12.4803595253),
	 48->List(0.108695822347,42.665037517,12.583652497),
	 49->List(0.10434746923,43.4939449548,12.639045822),
	 50->List(0.110229145762,44.4619854008,12.8973332522),
	 51->List(0.111140250556,45.4285368538,13.0297445093),
	 52->List(0.112367108275,46.6372249927,12.9002466982),
	 53->List(0.111143181894,47.5362469204,13.0501359849),
	 54->List(0.114040027948,48.4920139383,13.1254768717),
	 55->List(0.114217794125,49.5552500114,13.37428257),
	 56->List(0.115461287358,50.2348788411,13.491015054),
	 57->List(0.118309850904,51.2567277201,13.6522827307),
	 58->List(0.123714609027,52.1948771348,14.2550528223),
	 59->List(0.118256226364,53.0406027309,14.2575364116),
	 60->List(0.122323761855,53.9384443312,14.4097777824),
	 61->List(0.124949783017,54.8098954823,14.6019376371),
	 62->List(0.118345617444,55.6477153047,14.6823510901),
	 63->List(0.120439125622,57.1258231999,14.6131338891),
	 64->List(0.132770219518,58.0352780246,14.7674980951),
	 65->List(0.124562502509,58.8963150714,14.9214252935),
	 66->List(0.125904623877,59.8700119853,15.0462284),
	 67->List(0.130324296875,60.6954600394,15.1243343171),
	 68->List(0.123647734235,61.7378238583,15.2393542096),
	 69->List(0.130757770855,62.6822355173,15.8133923064),
	 70->List(0.127112442834,63.5098077631,15.7762086795),
	 71->List(0.13126920091,64.3406754181,15.9222392279),
	 72->List(0.126991066495,65.2599826521,16.0046053336),
	 73->List(0.129544821961,66.1422704512,16.2039837857),
	 74->List(0.130170779962,67.6113205042,16.1365087173),
	 75->List(0.126010701195,68.3222186259,16.2538767493),
	 76->List(0.123651111643,69.2457441159,16.4057614826),
	 77->List(0.129645406233,70.2065652102,16.4150106149),
	 78->List(0.139520581436,71.2199690049,16.6973239941),
	 79->List(0.129332754071,72.0821714901,16.7631742056),
	 80->List(0.139332910362,73.1178932851,17.2362010822),
	 81->List(0.117093326844,73.8417627221,17.1522534346),
	 82->List(0.115289581674,74.6268374001,17.3400369413),
	 83->List(0.132014886156,75.678752802,17.4310466),
	 84->List(0.12371176966,76.4785661536,17.4775970917),
	 85->List(0.135623333297,78.0135280026,17.5726529615),
	 86->List(0.139852459387,78.9260192313,17.6457917571),
	 87->List(0.135362231758,79.865367848,17.8313961616),
	 88->List(0.137577836132,80.8207735241,17.9103954098),
	 89->List(0.140910294593,81.7705819518,18.0650636021),
	 90->List(0.128389594772,82.5725911888,18.0752555116),
	 91->List(0.144160732428,83.7747594261,18.5285610235),
	 92->List(0.118186018867,84.2627484903,18.4174779855),
	 93->List(0.129393566958,85.2866424945,18.6503561616),
	 94->List(0.126956993636,86.0484470789,18.6849779751),
	 95->List(0.125172085383,87.0827592548,18.7861323238),
	 96->List(0.133153412188,88.4941785808,18.8135460884),
	 97->List(0.125274128804,89.3432298168,18.9039615471),
	 98->List(0.137586478014,90.3378166668,19.0765357793),
	 99->List(0.127807142733,91.2169987931,19.0472977646),
	 100->List(0.144342039282,92.1476347692,19.3281004953)
  ) 
  
  /**
   * a table modelling legend point costs when learning talents
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
   * a map modeling legend point costs when increasing attributes
   */
  val AtrributeIncreaseCost = Map(1 -> 800, 2 -> 1300, 3 -> 2100, 4 -> 3400, 5 -> 5500)

  /**
   * a map modelling legend point costs when increasing skills
   */
  val SkillIncreaseCost = Map(1 -> 200, 2 -> 300, 3 -> 500, 4 -> 800, 5 -> 1300, 6 -> 2100,
    7 -> 3400, 8 -> 5500, 9 -> 8900, 10 -> 14400)

  /**
   * a list giving MovementRates
   */
  val MovementRate = List("25/13","28/14","30/15","32/16","35/18","38/19",
		  				  "40/20","43/22","48/24","50/25","54/27","57/29",
		  				  "60/30","65/33","70/35","75/38","80/40","85/42",
		  				  "90/45","100/50","110/55","120/60","130/65","140/70",
		  				  "150/75","160/80","170/85","180/90","200/100",
		  				  "220/110"
		  				  )
  /**
   * a list giving MovementRates
   */
  val CarrCap = List("10/20","15/30","20/40","25/50","30/65","35/75",
		  				  "40/85","50/100","60/115","70/135","80/160","90/185",
		  				  "105/210","125/250","145/290","165/310","200/400","230/460",
		  				  "270/540","315/630","360/735","430/860","500/1000","580/1160",
		  				  "675/1350","790/1580","920/1840","1075/2150","1200/2500",
		  				  "1450/2900"
		  				  )
		  				  
  /**
   * a map providing the seed for step to dice conversion
   */
  val DiceStep = Map(
    0 -> "0",
    1 -> "d4+-2",
    2 -> "d4+-1",
    3 -> "1d4",
    4 -> "1d6",
    5 -> "1d8",
    6 -> "1d10",
    7 -> "1d12",
    8 -> "1d6+1d6",
    9 -> "1d8+1d6",
    10 -> "1d10+1d6",
    11 -> "1d10+1d8",
    12 -> "1d10+1d10",
    13 -> "1d12+1d10")
  
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

    if (List(0, 1, 2) contains modu)  base * 3 + 4
    else if (List(3, 4) contains modu) base * 3 + 5
    else return base * 3 + 6
  }

  /**
   * get the movement assoatiated with a given attribute value
   *
   * @param attr the value of the atrribute
   * @return the movement rating
   */
  def getAttrMovement(attr: Int): String = {
	  MovementRate(attr-1)
  }

  /**
   * get the carrying capacity assoatiated with a given attribute value
   *
   * @param attr the value of the atrribute
   * @return the carrying rating
   */
  def getAttrCarrying(attr: Int): String = {
    CarrCap (attr-1)
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
    if (attr < 3) 0.5
    else if (attr < 8) 1
    else return (attr - 8) / 6 + 2
  }

  /**
   * get the mystic armor
   *
   * @param attr the value of the atrribute
   * @return mystic armor
   */
  def getAttrMystic(attr: Int): Int = {
    if (attr < 11)  0
    else (attr - 11) / 3 + 1
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
    TalenRankCost(rank - 1)(circle_bin)
  }

  /**
   * filter the durability talent and return its values as a list of 2 integers
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

    if (durs.length > 0) {
      val durability = durs.head
      val talent = durability(2).asInstanceOf[Option[Int]]
      if (talent != None) {
        List(durability(0).asInstanceOf[Int] * durability(2).asInstanceOf[Option[Int]].get,
          durability(1).asInstanceOf[Int] * durability(2).asInstanceOf[Option[Int]].get)
      } else {
        List(0, 0)
      }
    } else { return List(0, 0) }
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
    var first = true
    talents.filter(talent =>
      if (!(talent.name contains "Dura")) {
        true
      } else if (first) {
        first = false
        true
      } else {
        false
      })
  }

  /**
   * Return the Dice corresponding to the step number
   *
   * @param step The Step Number
   * @return An Option with dice string
   */
  def getDiceForStep(step: Int): Option[String] = {
    if (step < 0) None
    if (step < 14) Some(DiceStep(step))
    else if (step < 25) Some("1d20+" + DiceStep(step - 11))
    else Some("%sd20+".format((step - 14) / 11) + "1d10+1d8+" + DiceStep((step - 14) % 11 + 3))
  }

  /**
   * Return the result of rolling a dice with the indicated number of sides
   *
   * @param dice the sides of the dice
   * @return number rolled
   */
  def rollDice(dice: Int): Int = {
    if (!(dice > 1)) 0
    val result = rand_gen.nextInt(dice) + 1
    if (result == dice) result + rollDice(dice)
    else result
  }

  /**
   * Return the result of rolling dices according to the dice string
   *
   *
   * @param dice_string the dice string
   * @return number rolled
   */
  def rollDiceString(dice_string: String): Int = {
    val pattern_1 = """(\d+)d(\d+)""".r
    val pattern_2 = """^d(\d+)""".r
    val pattern_3 = """s(\d+)""".r
    val pattern_4 = """^(\d+)$""".r
    val pattern_5 = """^-(\d+)$""".r
    dice_string.split("\\+").map(instruct => instruct match {
      case pattern_1(times, sides) => 1.to(times.toInt).
        map(x => rollDice(sides.toInt))
        .reduce((a1, a2) => a1 + a2)
      case pattern_2(sides) => rollDice(sides.toInt)
      case pattern_3(step) => rollDiceString(getDiceForStep(step.toInt)
        .get)
      case pattern_4(value) => value.toInt
      case pattern_5(value) => -1 * value.toInt
      case default => {
        Logger.error("I couldnt make sense of a dice instruction: %s".format(default))
        0
      }
    })
      .reduce((a1, a2) => a1 + a2)
  }

  def getRandomName() = {
    "NoName%sPleaseChangeME".format(rand_gen.nextInt(100000))
  }

   
  /**
   * Return the probability of reaching a certain value with a given step 
   *
   *
   * @param value the value to reach
   * @param step the step used
   * @return number rolled
   */
  def getProbabilityWithStep(value:Int,step:Int) = {
    val params = CdfParamters(step)
    1-generalizedExtremeValueCDF(value-1, params(1), params(2), -1*params(0))
  }

    /**
   * Return the cummulative density value for a generalized Extrem value 
   * Distribution 
   *
   *
   * @param x the point at which we evaluate the CDF
   * @param nu the mean
   * @param sigma first moment
   * @param eta the shape parameter
   * @return cdf value
   */
  def generalizedExtremeValueCDF(x:Float,nu:Double,sigma:Double,eta:Double) = {
    if(eta==0){
      scala.math.pow(scala.math.E, -scala.math.pow(scala.math.E,-(x-nu)/sigma))
    	}
    else{
      scala.math.pow(scala.math.E,-scala.math.pow(1+eta*((x-nu)/sigma),-1.0/eta))
    }
  }
}
