package org.scalagios.graph

import scala.collection.JavaConverters._
import org.openrdf.rio.RDFFormat
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
  
  def uri: String = vertex.getPropertyAsString(DATASET_URI)
  
  def title: String = vertex.getPropertyAsString(DATASET_TITLE)
  
  def description: String = vertex.getPropertyAsString(DATASET_DESCRIPTION)
  
  def license: String = vertex.getPropertyAsString(DATASET_LICENSE)
  
  def homepage: String = vertex.getPropertyAsString(DATASET_HOMEPAGE)
  
  var datadump: String = _
  
  var dumpFormat: RDFFormat = _
  
  var uriSpace: String = _
  
  def parent: Option[DatasetVertex] = {
    val parentVertex = vertex.getInEdges(RELATION_SUBSET).iterator()
    if (parentVertex.hasNext())
      Some(new DatasetVertex(parentVertex.next().getOutVertex()))
    else
      None
  } 
  
  def subsets: List[DatasetVertex] = 
    vertex.getOutEdges(RELATION_SUBSET).asScala.map(edge => new DatasetVertex(edge.getInVertex())).toList
    
  // TODO implement 'annotations' method on DatasetVertex
  def annotations: Iterable[GeoAnnotationVertex] = List.empty[GeoAnnotationVertex]

}