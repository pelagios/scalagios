package org.scalagios.graph

import org.scalagios.api.Place
import org.scalagios.graph.Constants._
import org.scalagios.graph.VertexExtensions._
import com.tinkerpop.blueprints.pgm.Vertex

/**
 * An implementation of the Pelagios <em>Place</em> model primitive
 * backed by a Tinkerpop Graph Vertex.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class PlaceVertex(vertex: Vertex) extends Place {
  
  def uri: String = vertex.getPropertyAsString(PLACE_URI).get
  
  def label = vertex.getPropertyAsString(PLACE_LABEL)
  
  def comment = vertex.getPropertyAsString(PLACE_COMMENT)
  
  def altLabels = vertex.getPropertyAsString(PLACE_ALTLABELS)
  
  def coverage = vertex.getPropertyAsString(PLACE_COVERAGE)
  
  def featureType = vertex.getPropertyAsString(PLACE_FEATURE_TYPE)
  
  def lon: Double = vertex.getPropertyAsDouble(PLACE_LON)
  
  def lat: Double = vertex.getPropertyAsDouble(PLACE_LAT)
  
  lazy val within = vertex.getFirstOutNeighbour(RELATION_WITHIN).map(new PlaceVertex(_))
  
  lazy val connectsWith =
    (vertex.getAllInNeighbours(RELATION_CONNECTS_WITH) ++
     vertex.getAllOutNeighbours(RELATION_CONNECTS_WITH)).map(new PlaceVertex(_))
  
  lazy val isDuplicateOf = vertex.getFirstOutNeighbour(RELATION_SAMEAS).map(new PlaceVertex(_)) 
  
  lazy val duplicates = vertex.getAllInNeighbours(RELATION_SAMEAS).map(new PlaceVertex(_))
  
  def geometryWKT = vertex.getPropertyAsString(PLACE_GEOMETRY)

}