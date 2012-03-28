package org.scalagios.graph.io

import com.tinkerpop.blueprints.pgm.IndexableGraph
import org.scalagios.graph.Constants._
import com.tinkerpop.blueprints.pgm.{Vertex, IndexableGraph}
import org.scalagios.api.{Place, Dataset}

/**
 * Provides Pelagios-specific Graph DB I/O (read) features.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class PelagiosGraphReader[T <: IndexableGraph](graph: T) extends PelagiosGraphIOBase(graph) {

  def getPlaces(): List[Place] = {
    List[Place]()
  }
  
  def getDatasets(): List[Dataset] = {
    List[Dataset]()
  }
  
}