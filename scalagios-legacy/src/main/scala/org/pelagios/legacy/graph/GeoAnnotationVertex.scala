package org.pelagios.legacy.graph

import org.pelagios.legacy.api.GeoAnnotation
import org.pelagios.legacy.graph.Constants._
import org.pelagios.legacy.graph.VertexExtensions._
import com.tinkerpop.blueprints.pgm.Vertex
import org.pelagios.legacy.graph.exception.GraphIntegrityException

/**
 * An implementation of the Pelagios <em>GeoAnnotation</em> model primitive
 * backed by a Tinkerpop Graph Vertex.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class GeoAnnotationVertex(private[graph] val vertex: Vertex) extends GeoAnnotation {
  
  def uri: String = vertex.getPropertyAsString(ANNOTATION_URI).get
  
  def body: String = vertex.getPropertyAsString(ANNOTATION_BODY).get
  
  def target: GeoAnnotationTargetVertex = {
    val neighbour = vertex.getFirstOutNeighbour(RELATION_HASTARGET)
    if (neighbour.isDefined)
      new GeoAnnotationTargetVertex(neighbour.get)
    else      
      throw new GraphIntegrityException("Graph corrupt: annotation " + uri + " disconnected from target")
  }

  def title: Option[String] = vertex.getPropertyAsString(ANNOTATION_TITLE)
  
  def thumbnail: Option[String] = vertex.getPropertyAsString(ANNOTATION_THUMBNAIL)
  
}