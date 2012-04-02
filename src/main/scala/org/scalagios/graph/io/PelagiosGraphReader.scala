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
class PelagiosGraphReader[T <: IndexableGraph](graph: T) extends PelagiosGraphIOBase(graph) {

  /**
   * Returns an iterable over all Places in the Graph
   */
  def getPlaces(): Iterable[Place] = getVertices(PLACE_VERTEX).map(vertex => new PlaceVertex(vertex))
  
  /**
   * Returns the Place with the specified URI
   */
  def getPlace(uri: String): Option[Place] = {
    val idxHits = placeIndex.get(PLACE_URI, uri)
    
    if (idxHits.hasNext())
      Some(new PlaceVertex(idxHits.next()))
    else
      None
  }

  /**
   * Returns an iterable over all top-level datasets in the Graph
   */
  def getDatasets(): List[Dataset] =
    datasetIndex.get(DATASET_URI, VIRTUAL_ROOT_URI).iterator.asScala.map(v => new DatasetVertex(v)).toList
  
  /**
   * Returns the Dataset with the specified URI
   */
  def getDataset(uri: String): Option[Dataset] = {
    val idxHits = datasetIndex.get(DATASET_URI, uri)
    
    if (idxHits.hasNext())
      // Some(framesManager.frame(idxHits.next(), classOf[DatasetVertex]))
      None
    else
      None    
  }
  
  /**
   * Returns an iterable over all datasets in the Graph. (Used for unit testing only!)
   */
  protected def listAllDatasets(): Iterable[Dataset] = getVertices(DATASET_VERTEX).map(vertex => new DatasetVertex(vertex))
  
  /**
   * I have the feeling this might become really slow in a full database. Even though this
   * method is probably used very rarely, we'll need to find a solution that scales better!
   */
  private def getVertices(vertexType: String) = graph.getVertices().asScala.filter(_.getProperty(VERTEX_TYPE).equals(vertexType))
  
}