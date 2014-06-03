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
  
  def start: Date
  
  def end: Option[Date]
  
  def name: Option[String]
  
}

/** Default POJO-style implementation of 'PeriodOfTime' **/
private[api] class DefaultPeriodOfTime(val start: Date, val end: Option[Date], val name: Option[String]) extends PeriodOfTime

/** Companion object with a pimped apply method for generating DefaultPeriodOfTime instances **/
object PeriodOfTime extends AbstractApiCompanion {
  
  def apply(start: Date, end: Option[Date] = None, name: Option[String] = None) = new DefaultPeriodOfTime(start, end, name)
  
  def fromString(str: String): PeriodOfTime = {
    
    def setEra(y: Int, cal: Calendar) = 
      if (y < 0)
        cal.set(Calendar.ERA, GregorianCalendar.BC)
      else
        cal.set(Calendar.ERA, GregorianCalendar.AD)
    
    val fields = str.split(";").map(_.trim)
    val start = fields.find(_.startsWith("start=")).map(_.substring(6).toInt)
    val end = fields.find(_.startsWith("end=")).map(_.substring(4).toInt)
    val name = fields.find(_.startsWith("name=")).map(_.substring(5))
    
    if (start.isEmpty)
      throw new IllegalArgumentException("Not a parsable time period: " + str)
    
    val calendar = Calendar.getInstance()
    
    calendar.set(Calendar.YEAR, start.get);
    setEra(start.get, calendar)
      
    val startDate = calendar.getTime()
    val endDate = end.map(e => {
      calendar.set(Calendar.YEAR, e)
      setEra(e, calendar)  
      calendar.getTime()
    })
    
    PeriodOfTime(startDate, endDate, name)
  }
  
}