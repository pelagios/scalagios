package org.scalagios.graph.io

import scala.collection.JavaConverters._
import com.tinkerpop.blueprints.pgm.IndexableGraph
import org.scalagios.graph.Constants._
import com.tinkerpop.blueprints.pgm.{Vertex, IndexableGraph}
import org.scalagios.api.{Place, Dataset}
import org.scalagios.graph.{PlaceVertex, DatasetVertex}

/**
 * Provides Pelagios-specific Graph DB I/O (read) features.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class PelagiosGraphReader[T <: IndexableGraph](val graph: T) extends PelagiosGraphIOBase {

  // TODO class should probably be broken up into smaller chunks, just like the writers
  
  /**
   * Returns an iterable over all Places in the Graph
   */
  def getPlaces(): Iterable[Place] = getVertices(PLACE_VERTEX).map(vertex => new PlaceVertex(vertex))
  
  /**
   * Returns the Place with the specified URI, if it exists in the Graph
   */
  def getPlace(uri: String): Option[Place] = {
    val idxHits = placeIndex.get(PLACE_URI, uri)
    
    if (idxHits.hasNext())
      Some(new PlaceVertex(idxHits.next()))
    else
      None
  }
  
  def getReferencedPlaces(datasetUri: String): Iterable[(Place, Int)] = { 
    val idxHits = datasetIndex.get(DATASET_URI, datasetUri)
    if (idxHits.hasNext())
      idxHits.next.getOutEdges(RELATION_REFERENCES).asScala
        .map(edge => (new PlaceVertex(edge.getInVertex) -> edge.getProperty(REL_PROPERTY_REFERENCECOUNT).toString.toInt))
    else
      Seq.empty[(Place, Int)]
  }
  
  def getReferencingDatasets(placeUri: String): Iterable[(Dataset, Int)] = {
    val idxHits = placeIndex.get(PLACE_URI, placeUri)
    if (idxHits.hasNext())
      idxHits.next.getInEdges(RELATION_REFERENCES).asScala
        .map(edge => (new DatasetVertex(edge.getOutVertex) -> edge.getProperty(REL_PROPERTY_REFERENCECOUNT).toString.toInt))
    else
      Seq.empty[(Dataset, Int)]
  }

  /**
   * Returns a list of the (top-level) datasets in the Graph
   */
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
    
  // TODO Might be slow in a large DB! Although we'll probably use this rarely, we'll need a more scalable solution
  private[io] def getVertices(vertexType: String) = graph.getVertices().asScala.filter(_.getProperty(VERTEX_TYPE).equals(vertexType))
  
}