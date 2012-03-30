package org.scalagios.graph

import com.tinkerpop.blueprints.pgm.Vertex

object VertexUtils {
  
  implicit def wrapVertex(vertex: Vertex) = new VertexUtils(vertex)

}

class VertexUtils(vertex: Vertex) {
  
  def getPropertyAsString(key: String): String = {
    val property = vertex.getProperty(key)
    if (property == null) null else property.toString
  } 
  
  def getPropertyAsDouble(key: String): Double = {
    val property = getPropertyAsString(key)
    if (property == null) 0 else property.toDouble
  }
  
  def getNeighbour(relation: String): Option[Vertex] = {
    val outEdges = vertex.getOutEdges(relation).iterator
    if (outEdges.hasNext())
      Some(outEdges.next().getInVertex())
    else
      None
  }

}