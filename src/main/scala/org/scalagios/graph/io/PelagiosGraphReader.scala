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

  def getPlaces(): Iterable[Place] = getVertices(PLACE_VERTEX).map(vertex => framesManager.frame(vertex, classOf[PlaceVertex]))
  
  def getPlace(uri: String): Place = null // TODO

  def getDatasets(): Iterable[Dataset] = getVertices(DATASET_VERTEX).map(vertex => framesManager.frame(vertex, classOf[DatasetVertex]))

  def getDataset(uri: String): Dataset = null // TODO
  
  private def getVertices(vertexType: String) = graph.getVertices().asScala.filter(_.getProperty(VERTEX_TYPE).equals(vertexType))
  
}