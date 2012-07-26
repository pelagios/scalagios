package org.scalagios.graph

import scala.collection.JavaConverters._
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
  
  def getPropertyAsLong(key: String): Option[Long] = {
    val property = vertex.getProperty(key)
    if (property == null) None else Some(property.toString.toLong)
  }
  
  def getFirstInNeighbour(relation: String): Option[Vertex] = {
    val inEdges = vertex.getInEdges(relation).iterator
    if (inEdges.hasNext())
      Some(inEdges.next().getOutVertex())
    else
      None    
  }
  
  def getFirstOutNeighbour(relation: String): Option[Vertex] = {
    val outEdges = vertex.getOutEdges(relation).iterator
    if (outEdges.hasNext())
      Some(outEdges.next().getInVertex())
    else
      None
  }
  
  def getAllInNeighbours(relation: String): Seq[Vertex] = 
    vertex.getInEdges(relation).iterator.asScala.map(_.getOutVertex).toSeq 
  
  def getAllOutNeighbours(relation: String): Seq[Vertex] =
    vertex.getOutEdges(relation).iterator.asScala.map(_.getInVertex).toSeq

}

object VertexExtensions {
  
  implicit def wrapVertex(vertex: Vertex) = new VertexExtensions(vertex)

}
