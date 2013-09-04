package org.pelagios.legacy.graph

import org.pelagios.legacy.graph.Constants._
import org.pelagios.legacy.graph.VertexExtensions._
import org.pelagios.legacy.api.GeoAnnotationTarget
import com.tinkerpop.blueprints.pgm.Vertex

/**
 * An implementation of the Pelagios <em>AnnotationTarget</em> model primitive
 * backed by a Tinkerpop Graph Vertex.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class GeoAnnotationTargetVertex(vertex: Vertex) extends GeoAnnotationTarget {
  
  def uri: String = vertex.getPropertyAsString(ANNOTATION_TARGET_URI).get

  def title: Option[String] = vertex.getPropertyAsString(ANNOTATION_TARGET_TITLE)
  
  def thumbnail: Option[String] = vertex.getPropertyAsString(ANNOTATION_TARGET_THUMBNAIL)

}