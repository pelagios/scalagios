package org.scalagios.graph

import com.tinkerpop.frames.Property
import org.scalagios.api.GeoAnnotation

/**
 * An implementation of the Pelagios <em>GeoAnnotation</em> model primitive
 * backed by a Tinkerpop Graph Vertex.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait GeoAnnotationVertex extends GeoAnnotation {
  
  @Property("uri")
  def uri: String
  
  @Property("title")
  def title: String
  
  @Property("body")
  def body: String
  
  // TODO wire this up with the relation to the target vertex
  def target: AnnotationTargetVertex;

}