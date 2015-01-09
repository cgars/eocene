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
package models

import play.api.libs.json.Format
import play.api.libs.json._
import eoceneServices.eoceneSqlStrings
import anorm._
import play.api.db.DB
import play.api.Play.current
import java.sql.Connection
import play.api.Logger
/**
 * Armor
 *
 */
case class Armor(val id: Int, name: String, cost: Int, init: Int, phys: Int, mystic: Int,
  shatter: Option[Int], powers: List[Power], threads: Option[Int]) {

  /**
   * Get the mystical armor value that this item provides (including threaded powers)
   *
   * @return the mystical armor bonus
   */
  def getMysticalArmor() = {
    mystic +
      powers.filter(power => power.effect == "Mystic Armor")
      .map(power => power.value)
      .reduceOption((p1, p2) => if (p1 > p2) p1 else p2).
      getOrElse(0)
  }

  /**
   * Get the physical armor value that this item provides (including threaded powers)
   *
   * @return the physical armor bonus
   */
  def getPhysicalArmor() = {
    phys +
      powers.filter(power => power.effect == "Physic Armor")
      .map(power => power.value).reduceOption((p1, p2) => if (p1 > p2) p1 else p2)
      .getOrElse(0)
  }

  /**
   * Get the ini value that this item provides (including threaded powers)
   *
   * @return the ini bonus
   */
  def getInitiativeBonus() = {
    init +
      powers.filter(power => power.effect == "Initiative")
      .map(power => power.value)
      .reduceOption((p1, p2) => if (p1 > p2) p1 else p2).getOrElse(0)
  }

}

/**
 * Mainly a factory for Armors (using rows from a db querry)
 */
object Armor {

  /**
   * Get a Armor
   * @param rows the rows from a dab call each row might correspond to a power
   * @return Armor
   */
  def getArmorByRows(rows: Stream[anorm.Row])(implicit c: Connection) = {
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

  implicit object RaceFormat extends Format[Armor] {

    def reads(json: JsValue) = JsSuccess(getArmorById(1))

    def writes(armor: Armor) = JsObject(Seq(
      "id" -> JsNumber(armor.id),
      "name" -> JsString(armor.name),
      "cost" -> JsNumber(armor.cost),
      "init" -> JsNumber(armor.init),
      "phys" -> JsNumber(armor.phys),
      "mystic" -> JsNumber(armor.mystic),
      "shatter" -> JsNumber(armor.shatter.get),
      "powers" -> Json.toJson(armor.powers)))
  }

}
