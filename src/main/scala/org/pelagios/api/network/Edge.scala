package org.pelagios.api.network

import org.pelagios.api.Annotation
import org.pelagios.api.AbstractApiCompanion

/** Represents neighbourhood between two annotations in an annotation sequence or topology. 
  *
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait Edge {
  
  /** The Annotation that is the start node of the edge **/
  def from: Annotation
  
  /** The Annotation that is the end node of the edge **/
  def to: Annotation

  /** If true, the direction of the edge is relevant **/
  def directional: Boolean
  
  /** pelagios:neighbourDistance - the distance (if applicable) **/
  def distance: Option[Double]
  
  /** pelagios:distanceUnit - the unit distance is measured in **/
  def unit: Option[String] 
  
}

/** A default POJO-style implementation of Neighbour. **/
private[network] class DefaultEdge(
    
    val from: Annotation,
    
    val to: Annotation,
    
    val directional: Boolean = false, 
    
    val distance: Option[Double] = None, 
    
    val unit: Option[String] = None
    
) extends Edge
  
/** Companion object with a pimped apply method for generating DefaultEdge instances **/
object Edge extends AbstractApiCompanion {
 
  def apply(from: Annotation, to: Annotation,
            
            directional: Boolean = false,
            
            distance: Option[Double] = None,
            
            unit: String = null): Edge = {
    
    new DefaultEdge(from, to, directional, distance, unit)
  }
 
}
