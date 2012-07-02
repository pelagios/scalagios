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
  
  val within = {
    val containingPlace = vertex.getOutNeighbour(RELATION_WITHIN)
    if (containingPlace.isDefined)
      Some(new PlaceVertex(containingPlace.get))
    else
      None
  }
  
  def geometryWKT = vertex.getPropertyAsString(PLACE_GEOMETRY)

}