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
  
  def uri = vertex.getPropertyAsString(DATASET_URI).get
  
  def context = vertex.getPropertyAsString(DATASET_CONTEXT).get
  
  def title = vertex.getPropertyAsString(DATASET_TITLE).get
  
  def description = vertex.getPropertyAsString(DATASET_DESCRIPTION)
  
  def license = vertex.getPropertyAsString(DATASET_LICENSE)
  
  def homepage = vertex.getPropertyAsString(DATASET_HOMEPAGE)
  
  def associatedDatadumps = {
    val dumpList = vertex.getPropertyAsString(DATASET_DATADUMP)
    if (dumpList.isDefined)
      dumpList.map(_.split(",")).flatten.toList
    else
      List.empty[String]
  }
  
  def associatedUriSpace = vertex.getPropertyAsString(DATASET_URISPACE)
  
  def associatedRegexPattern = vertex.getPropertyAsString(DATASET_URIREGEXPATTERN)
  
  def subsets: Iterable[DatasetVertex] = 
    vertex.getOutEdges(RELATION_SUBSET).asScala.map(edge => new DatasetVertex(edge.getInVertex()))
    
  def _listAnnotations(hasBody: Option[String]): Iterable[GeoAnnotationVertex] = {
    val annotations = vertex.getOutEdges(RELATION_CONTAINS).asScala.map(edge => new GeoAnnotationVertex(edge.getInVertex))
    if (hasBody.isDefined)
      annotations.filter(_.body.equals(hasBody))
    else
      annotations
  }
   
}