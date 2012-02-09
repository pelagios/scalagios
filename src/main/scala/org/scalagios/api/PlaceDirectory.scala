package org.scalagios.api

/**
 * A class with the PlaceDirectory trait provides simple
 * lookup functionality for Places by URI. 
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait PlaceDirectory {

  def getPlace[T <: Place](uri: String): T
  
}