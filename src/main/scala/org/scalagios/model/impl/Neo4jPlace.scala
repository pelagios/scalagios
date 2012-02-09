package org.scalagios.model.impl

import com.tinkerpop.frames.Property

trait Neo4jPlace {
  
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