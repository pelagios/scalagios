package org.pelagios.api

/** 'Neighbour' model primitive.
  *
  * @constructor create a new neighbour
  * @param annotation the neighbouring annotation
  * @param directional set to true if the direction of the link is relevant (defaults to false)
  * @param distance the distance (if applicable, defaults to None)
  * @param unit the unit distance is measured in (defaults to None)
  * @author Rainer Simon <rainer.simon@ait.ac.at> 
  */
class Neighbour(val annotation: Annotation, val directional: Boolean = false, val distance: Option[Double] = None, val unit: Option[String] = None) {
  
  /** Tests if the Neighbour has directional, distance or unit metadata.
    * 
    * This flag is helpful for serialization: if the serializer knows that the neighbour
    * has no metadata, it can insert a plain URI, otherwise a blank node.) Since we need to 
    * update the method whenever the [[Neighbour]] class changes, we keep it in here rather
    * than in the serializer.
    */
  lazy val hasMetadata: Boolean = distance.isDefined || unit.isDefined 
  
}