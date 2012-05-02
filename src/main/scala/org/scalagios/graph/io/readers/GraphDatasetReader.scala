package org.scalagios.graph.io.readers

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import com.tinkerpop.blueprints.pgm.Vertex
import org.scalagios.api.{Dataset, Place}
import org.scalagios.graph.Constants._
import org.scalagios.graph.VertexExtensions._
import org.scalagios.graph.{DatasetVertex, PlaceVertex}
import org.scalagios.graph.io.PelagiosGraphIOBase

trait GraphDatasetReader extends PelagiosGraphIOBase {

  def getDatasets(): List[Dataset] =
    datasetIndex.get(DATASET_URI, VIRTUAL_ROOT_URI).iterator.asScala.map(v => new DatasetVertex(v)).toList
  
  def getDataset(uri: String): Option[Dataset] = {
    val idxHits = datasetIndex.get(DATASET_URI, uri)
    if (idxHits.hasNext()) Some(new DatasetVertex(idxHits.next()))
    else None    
  }
  
  def findDatasetByHash(hash: String): Option[Dataset] = {
    val idxHits = datasetIndex.get(DATASET_HASH, hash)
    if (idxHits.hasNext()) Some(new DatasetVertex(idxHits.next()))
    else None       
  }
  
  def getReferencedPlaces(datasetUri: String): Iterable[(Place, Int)] = { 
    val idxHits = datasetIndex.get(DATASET_URI, datasetUri)
    if (idxHits.hasNext())
      idxHits.next.getOutEdges(RELATION_REFERENCES).asScala
        .map(edge => (new PlaceVertex(edge.getInVertex) -> edge.getProperty(REL_PROPERTY_REFERENCECOUNT).toString.toInt))
    else
      Seq.empty[(Place, Int)]
  }
  
  def getDatasetHierarchy(dataset: Dataset): List[Dataset] = {
    _traverseHierarchy(dataset.asInstanceOf[DatasetVertex].vertex, ListBuffer.empty[Dataset]).toList
  }
  
  private def _traverseHierarchy(dataset: Vertex, hierarchy: ListBuffer[Dataset]): ListBuffer[Dataset] = {
    dataset.getInNeighbour(RELATION_SUBSET) match {
      case Some(vertex) => {
        hierarchy.append(new DatasetVertex(vertex))
        _traverseHierarchy(vertex, hierarchy) 
      }
      case None => hierarchy
    }
  } 

}