package org.scalagios.api

import scala.collection.mutable.ListBuffer

/**
 * A class with the PlaceIndex trait provides simple
 * lookup functionality for Places by URI. 
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait PlaceIndex {

  def getPlace(uri: String): Option[Place]
  
}

/**
 * A default implementation of a PlaceIndex based on wrapping
 * a simple ListBuffer. (<strong>Note:</strong> This is for
 * test/development only - not particularly efficient.)
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class DefaultPlaceIndex(places: ListBuffer[Place]) extends PlaceIndex {
  
  def getPlace(uri: String): Option[Place] = places.find(place => place.uri.equals(uri))
  
}