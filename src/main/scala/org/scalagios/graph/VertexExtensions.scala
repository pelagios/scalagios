package org.scalagios.graph

import com.tinkerpop.blueprints.pgm.Vertex

/**
 * A wrapper that provides useful additions to the default
 * Tinkerpop Vertex class (for use with Implicit Conversion).
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class VertexExtensions(vertex: Vertex) {
  
  def getPropertyAsString(key: String): Option[String] = {
    val property = vertex.getProperty(key)
    if (property == null) None else Some(property.toString)
  } 
  
  def getPropertyAsDouble(key: String): Double = {
    val property = vertex.getProperty(key)
    if (property == null) Double.NaN else property.toString.toDouble
  }
  
  def getNeighbour(relation: String): Option[Vertex] = {
    val outEdges = vertex.getOutEdges(relation).iterator
    if (outEdges.hasNext())
      Some(outEdges.next().getInVertex())
    else
      None
  }

}

object VertexExtensions {
  
  implicit def wrapVertex(vertex: Vertex) = new VertexExtensions(vertex)

}
