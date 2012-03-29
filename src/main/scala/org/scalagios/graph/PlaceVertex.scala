package org.scalagios.graph

import org.scalagios.api.Place
import org.scalagios.graph.Constants._
import org.scalagios.graph.VertexUtils._
import com.tinkerpop.blueprints.pgm.Vertex

/**
 * An implementation of the Pelagios <em>Place</em> model primitive
 * backed by a Tinkerpop Graph Vertex.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class PlaceVertex(vertex: Vertex) extends Place {
  
  def uri: String = vertex.getPropertyAsString(PLACE_URI)
  
  def label: String = vertex.getPropertyAsString(PLACE_LABEL)
  
  def comment: String = vertex.getPropertyAsString(PLACE_COMMENT)
  
  def altLabels: String = vertex.getPropertyAsString(PLACE_ALTLABELS)
  
  def lon: Double = vertex.getPropertyAsDouble(PLACE_LON)
  
  def lat: Double = vertex.getPropertyAsDouble(PLACE_LAT)
  
  val within: String = vertex.getPropertyAsString(PLACE_WITHIN)
  
  def geometryWKT: String = vertex.getPropertyAsString(PLACE_GEOMETRY)

}