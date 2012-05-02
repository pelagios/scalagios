package org.scalagios.graph.io.readers

import scala.collection.JavaConverters._
import org.scalagios.api.{Dataset, Place}
import org.scalagios.graph.Constants._
import org.scalagios.graph.{DatasetVertex, PlaceVertex}
import org.scalagios.graph.io.PelagiosGraphIOBase

trait GraphDatasetReader extends PelagiosGraphIOBase {

  def getDatasets(): List[Dataset] =
    datasetIndex.get(DATASET_URI, VIRTUAL_ROOT_URI).iterator.asScala.map(v => new DatasetVertex(v)).toList
  
  /**
   * Returns the Dataset with the specified URI, if it exists in the graph
   */
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

}