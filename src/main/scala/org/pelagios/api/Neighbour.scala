package org.pelagios.api

/** 'Neighbour' model primitive. 
  *
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait Neighbour {
  
  /** The neighbouring annotation. **/
  def annotation: Annotation

  /** If true, the direction of the link is relevant. 
    *
    * In case the direction is relevant, the neighbour is considered
    * to be the "next" in the sequence.
    */
  def directional: Boolean
  
  /** pelagios:neighbourDistance - distance the distance (if applicable) **/
  def distance: Option[Double]
  
  /** pelagios:distanceUnit - the unit distance is measured in **/
  def unit: Option[String] 

  /** Tests if the Neighbour has directional, distance or unit metadata.
    * 
    * This flag is helpful for serialization: if the serializer knows that the neighbour
    * has no metadata, it can insert a plain URI, otherwise a blank node.) Since we need to 
    * update the method whenever the [[Neighbour]] class changes, we keep it in here rather
    * than in the serializer.
    */
  lazy val hasMetadata: Boolean = distance.isDefined || unit.isDefined
  
}

/** A default POJO-style implementation of Neighbour. **/
class DefaultNeighbour(
    
    val annotation: Annotation, 
    
    val directional: Boolean = false, 
    
    val distance: Option[Double] = None, 
    
    val unit: Option[String] = None) 
    
    extends Neighbour