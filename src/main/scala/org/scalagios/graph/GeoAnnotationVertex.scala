package org.scalagios.graph

import org.scalagios.api.GeoAnnotation
import org.scalagios.graph.Constants._
import org.scalagios.graph.VertexUtils._
import com.tinkerpop.blueprints.pgm.Vertex

/**
 * An implementation of the Pelagios <em>GeoAnnotation</em> model primitive
 * backed by a Tinkerpop Graph Vertex.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class GeoAnnotationVertex(vertex: Vertex) extends GeoAnnotation {
  
  def uri: String = vertex.getPropertyAsString(ANNOTATION_URI)
  
  def title: String = vertex.getPropertyAsString(ANNOTATION_TITLE)
  
  def body: String = vertex.getPropertyAsString(ANNOTATION_BODY)
  
  def target: GeoAnnotationTargetVertex = {
    val neighbour = vertex.getNeighbour(RELATION_HASTARGET)
    if (neighbour.isDefined)
      new GeoAnnotationTargetVertex(neighbour.get)
    else      
      throw new RuntimeException("Graph corrupt: annotation " + uri + " disconnected from target")
  }

}