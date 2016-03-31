package org.pelagios.api

import java.text.SimpleDateFormat
import java.util.{ Calendar, Date, GregorianCalendar, TimeZone }
import org.slf4j.LoggerFactory

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
  
  private def getYear(date: Date) = {
    val calendar = Calendar.getInstance()
    calendar.setTime(date)
    if (calendar.get(Calendar.ERA) == GregorianCalendar.AD)
      calendar.get(Calendar.YEAR)
    else
      - calendar.get(Calendar.YEAR)
  }
  
  def start: Date
  
  def startYear: Int = getYear(start)
  
  def end: Option[Date]
  
  def endYear: Option[Int] = end.map(getYear(_))
  
  def name: Option[String]
  
}

/** Default POJO-style implementation of 'PeriodOfTime' **/
private[api] class DefaultPeriodOfTime(val start: Date, val end: Option[Date], val name: Option[String]) extends PeriodOfTime

/** Companion object with a pimped apply method for generating DefaultPeriodOfTime instances **/
object PeriodOfTime {
  
  private val yMdFormat = new SimpleDateFormat("yyyy-MM-dd")
  
  private val yMFormat = new SimpleDateFormat("yyyy-MM")
  
  private val MINUS = "-"

  def apply(start: Date, end: Option[Date] = None, name: Option[String] = None) = new DefaultPeriodOfTime(start, end, name)
  
  /** Parses the YYYY-MM-DD format. Month & days are optional **/
  private def parseYYYYMMDD(str: String): Date = {
    // Minus acts as separating char, but leading minus is... just a minus, so separate!
    val (sgn, abs) =
      if (str.trim.startsWith(MINUS))
        (-1, str.trim.substring(1)) 
      else
        (1, str.trim)
    
    if (abs.contains(MINUS)) {
      val dateFormat = (abs.split(MINUS).size - 1) match {
        case 1 => yMFormat
        case _ => yMdFormat
      }
      dateFormat.parse(str)     
    } else {
      val cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"))
      cal.setGregorianChange(new Date(Long.MinValue))
      cal.setTimeInMillis(0)
      cal.set(Calendar.YEAR, abs.toInt * sgn)
      val date = cal.getTime
      date
    }
  }
  
  /** Note: we support DCMI Period Encoding scheme and <start>/<end> **/
  def fromString(str: String): PeriodOfTime = {    
    if (str.contains(";")) {
      // DCMI
      val fields = str.split(";").map(_.trim)
      val start = fields.find(_.startsWith("start=")).map(str => parseYYYYMMDD(str.substring(6)))
      val end = fields.find(_.startsWith("end=")).map(str => parseYYYYMMDD(str.substring(4)))
      val name = fields.find(_.startsWith("name=")).map(str => str.substring(5))

      if (end.isDefined && end.get.before(start.get))
        throw new NumberFormatException("End date before start date: " + str) 
        
      PeriodOfTime(start.get, end, name)
    } else {
      // <start>/<end>, with format YYYY[-MM-DD]
      val fields = str.split("/").map(_.trim)
      val start = parseYYYYMMDD(fields(0))
      val end = if (fields.size > 1) Some(parseYYYYMMDD(fields(1))) else None
      
      if (end.isDefined && end.get.before(start))
        throw new NumberFormatException("End date before start date: " + str) 
      
      PeriodOfTime(start, end)
    }
  }
  
}
