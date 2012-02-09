package org.scalagios.graph

import com.tinkerpop.frames.Property
import org.scalagios.api.Place

/**
 * An implementation of the Pelagios <em>Place</em> model primitive
 * backed by a Tinkerpop Graph Vertex.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait PlaceVertex extends Place {
  
  @Property("id")
  def id: String
  
  @Property("label")
  def label: String
  
  @Property("comment")
  def comment: String
  
  @Property("altLabels")
  def altLabels: String
  
  @Property("lon")
  def lon: Double
  
  @Property("lat")
  def lat: Double
  
  @Property("geometry")
  def geometryWKT: String

}