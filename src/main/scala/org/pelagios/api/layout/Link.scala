package org.pelagios.api.layout

import org.pelagios.api.Annotation
import org.pelagios.api.AbstractApiCompanion

/** Represents neighbourhood between two annotations in an annotation sequence or topology. 
  *
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait Link {
  
  /** The Annotation that is the start of the link **/
  def from: Annotation
  
  /** The Annotation that is the end of the link **/
  def to: Annotation

  /** If true, the direction of the link is relevant **/
  def directional: Boolean
  
  /** layout:distance - the distance (if applicable) **/
  def distance: Option[Double]
  
  /** layout:unit - the unit distance is measured in **/
  def unit: Option[String] 
  
}

/** A default POJO-style implementation of Neighbour. **/
private[layout] class DefaultLink(
    
    val from: Annotation,
    
    val to: Annotation,
    
    val directional: Boolean = false, 
    
    val distance: Option[Double] = None, 
    
    val unit: Option[String] = None
    
) extends Link
  
/** Companion object with a pimped apply method for generating DefaultLink instances **/
object Link extends AbstractApiCompanion {
 
  def apply(from: Annotation, to: Annotation,
            
            directional: Boolean = false,
            
            distance: Option[Double] = None,
            
            unit: String = null): Link = {
    
    new DefaultLink(from, to, directional, distance, unit)
  }
 
}
