package org.pelagios.api

/** 'Distance' model entity.
  *
  * When annotations are ordered according to their sequence index, an optional 'Distance'
  * resource can be used to track additional information about the distance between 
  * the two annotations. This is mostly to better support recording of (the common use
  * case of) itinerary documents. 
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>  
  */
trait Distance {
  
  def label: Option[Label]
  
  def weight: Double

}

/** A default POJO-style implementation of 'Weight' **/
private[api] class DefaultDistance(val label: Option[Label], val weight: Double) extends Distance

/** Companion object with pimped apply methods for generating DefaultDistance instances **/
object Distance extends AbstractApiCompanion {
  
  def apply(weight: Double) = new DefaultDistance(None, weight)
  
  def apply(label: Label, weight: Double = 1.0) = new DefaultDistance(label, weight)
  
}