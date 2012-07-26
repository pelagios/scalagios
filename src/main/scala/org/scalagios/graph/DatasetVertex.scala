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
case class DatasetVertex(private[graph] val vertex: Vertex)  extends Dataset {
  
  def uri = vertex.getPropertyAsString(DATASET_URI).get
  
  def rootUri = vertex.getPropertyAsString(DATASET_ROOTURI).get
  
  def lastUpdated = vertex.getPropertyAsLong(DATASET_LASTUPDATED).get
  
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
      annotations.filter(_.body.equals(hasBody.get))
    else
     annotations
  }
  
  def countAnnotations(nested: Boolean = false): Int = {
    if (nested == true)
      vertex.getPropertyAsDouble(DATASET_ANNOTATION_COUNT).toInt
    else
      _listAnnotations(None).size
  }
  
  def isChildOf(uri: String) = {
    val parent = vertex.getFirstInNeighbour(RELATION_SUBSET).map(new DatasetVertex(_))
    if (parent.isEmpty)
      false
    else
      if (parent.get.uri.equals(uri))
        true
      else
        parent.get.isChildOf(uri)
  }
    
}