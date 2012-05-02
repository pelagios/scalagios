package org.scalagios.graph.io

import com.tinkerpop.blueprints.pgm.IndexableGraph
import org.scalagios.graph.io.readers.{GraphDatasetReader, GraphPlaceReader}
import org.scalagios.graph.io.readers.GraphAnnotationReader

/**
 * Provides Pelagios-specific Graph DB I/O (read) features.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class PelagiosGraphReader[T <: IndexableGraph](val graph: T) 
  extends PelagiosGraphIOBase 
  with GraphDatasetReader with GraphPlaceReader with GraphAnnotationReader {
  
}