package org.pelagios.api

import java.util.Date
import java.util.Calendar
import java.util.GregorianCalendar

/** A period of time.
  *  
  * Defined according to the DCMI Period Encoding Scheme
  * http://dublincore.org/documents/dcmi-period/
  * 
  * @constructor
  * @param start the start date
  * @param end the end date; if none is specified, the period is treated as a singular timestamp
  * @param name an optional name for the period
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait PeriodOfTime {
  
  def start: Int
  
  def end: Option[Int]
  
  def name: Option[String]
  
}

/** Default POJO-style implementation of 'PeriodOfTime' **/
private[api] class DefaultPeriodOfTime(val start: Int, val end: Option[Int], val name: Option[String]) extends PeriodOfTime

/** Companion object with a pimped apply method for generating DefaultPeriodOfTime instances **/
object PeriodOfTime extends AbstractApiCompanion {
  
  def apply(start: Int, end: Option[Int] = None, name: Option[String] = None) = new DefaultPeriodOfTime(start, end, name)
  
  /** Note: we support DCMI Period Encoding scheme and <start>/<end> **/
  def fromString(str: String): PeriodOfTime = {
    if (str.contains(";")) {
      // DCMI
      val fields = str.split(";").map(_.trim)
      val start = fields.find(_.startsWith("start=")).map(_.substring(6).toInt)
      val end = fields.find(_.startsWith("end=")).map(_.substring(4).toInt)
      val name = fields.find(_.startsWith("name=")).map(_.substring(5))

      PeriodOfTime(start.get, end, name)
    } else {
      // <start>/<end>
      val fields = str.split("/").map(_.trim)
      val start = fields(0).toInt
      val end = if (fields.size > 1) Some(fields(1).toInt) else None
      PeriodOfTime(start, end)
    }
  }
  
}