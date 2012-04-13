package org.scalagios.graph

import scala.collection.JavaConverters._
import org.scalagios.api.Dataset
import org.scalagios.graph.Constants._
import org.scalagios.graph.VertexExtensions._
import com.tinkerpop.blueprints.pgm.Vertex

/**
 * An implementation of the Pelagios <em>Dataset</em> model primitive
 * backed by a Tinkerpop Graph Vertex.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class DatasetVertex(private[graph] val vertex: Vertex)  extends Dataset {
  
  def uri = vertex.getPropertyAsString(DATASET_URI)
  
  def context = vertex.getPropertyAsString(DATASET_CONTEXT)
  
  def title = vertex.getPropertyAsString(DATASET_TITLE)
  
  def description = Some(vertex.getPropertyAsString(DATASET_DESCRIPTION))
  
  def license = Some(vertex.getPropertyAsString(DATASET_LICENSE))
  
  def homepage = Some(vertex.getPropertyAsString(DATASET_HOMEPAGE))
  
  // TODO
  def associatedDatadumps = List.empty[String]
  
  def associatedUriSpace = Some(vertex.getPropertyAsString(DATASET_URISPACE))
  
  def associatedRegexPattern = Some(vertex.getPropertyAsString(DATASET_URIREGEXPATTERN))
  
  def subsets: Iterable[DatasetVertex] = 
    vertex.getOutEdges(RELATION_SUBSET).asScala.map(edge => new DatasetVertex(edge.getInVertex()))
    
  // TODO implement 'annotations' method on DatasetVertex
  def annotations: Iterable[GeoAnnotationVertex] = 
    List.empty[GeoAnnotationVertex]

}