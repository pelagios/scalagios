package org.scalagios.graph

import org.scalagios.graph.Constants._
import org.scalagios.graph.VertexExtensions._
import org.scalagios.api.GeoAnnotationTarget
import com.tinkerpop.blueprints.pgm.Vertex

/**
 * An implementation of the Pelagios <em>AnnotationTarget</em> model primitive
 * backed by a Tinkerpop Graph Vertex.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class GeoAnnotationTargetVertex(vertex: Vertex) extends GeoAnnotationTarget {
  
  def uri: String = vertex.getPropertyAsString(ANNOTATION_TARGET_URI).get

  def title: Option[String] = vertex.getPropertyAsString(ANNOTATION_TARGET_TITLE)

}