package org.scalagios.graph

import org.scalagios.api.AnnotationTarget
import com.tinkerpop.frames.Property

/**
 * An implementation of the Pelagios <em>AnnotationTarget</em> model primitive
 * backed by a Tinkerpop Graph Vertex.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait AnnotationTargetVertex extends AnnotationTarget {
  
  @Property("uri")
  def uri: String
  
  @Property("title")
  def title: String

}