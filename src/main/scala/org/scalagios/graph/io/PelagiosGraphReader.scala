package org.scalagios.graph.io

import scala.collection.JavaConverters._
import com.tinkerpop.blueprints.pgm.IndexableGraph
import org.scalagios.graph.Constants._
import com.tinkerpop.blueprints.pgm.{Vertex, IndexableGraph}
import org.scalagios.api.{Place, Dataset}
import org.scalagios.graph.{PlaceVertex, DatasetVertex}
import com.tinkerpop.frames.FramesManager

/**
 * Provides Pelagios-specific Graph DB I/O (read) features.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class PelagiosGraphReader[T <: IndexableGraph](graph: T) extends PelagiosGraphIOBase(graph) {
  
  private val framesManager: FramesManager = new FramesManager(graph)

  /**
   * Returns an iterable over all Places in the Graph
   */
  def getPlaces(): Iterable[Place] = getVertices(PLACE_VERTEX).map(vertex => framesManager.frame(vertex, classOf[PlaceVertex]))
  
  /**
   * Returns the Place with the specified URI
   */
  def getPlace(uri: String): Option[Place] = {
    val idxHits = placeIndex.get(PLACE_URI, uri)
    
    if (idxHits.hasNext())
      Some(framesManager.frame(idxHits.next(), classOf[PlaceVertex]))
    else
      None
  }

  /**
   * Returns an iterable over all datasets in the Graph
   */
  def getDatasets(): Iterable[Dataset] = getVertices(DATASET_VERTEX).map(vertex => framesManager.frame(vertex, classOf[DatasetVertex]))

  /**
   * Returns the Dataset with the specified URI
   */
  def getDataset(uri: String): Option[Dataset] = {
    val idxHits = datasetIndex.get(DATASET_URI, uri)
    
    if (idxHits.hasNext())
      Some(framesManager.frame(idxHits.next(), classOf[DatasetVertex]))
    else
      None    
  }
  
  private def getVertices(vertexType: String) = graph.getVertices().asScala.filter(_.getProperty(VERTEX_TYPE).equals(vertexType))
  
}