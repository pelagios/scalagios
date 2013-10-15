package org.pelagios.api

import java.util.Date

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
case class PeriodOfTime(val start: Date, val end: Option[Date] = None, val name: Option[String] = None)
