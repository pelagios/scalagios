package org.scalagios.model.impl

import com.tinkerpop.frames.Property

trait Neo4jGeoAnnotation {
  
  @Property("uri")
  def uri: String
  
  @Property("body")
  def body: String
  
  @Property("target")
  def target: String

}