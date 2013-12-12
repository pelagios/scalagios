package org.pelagios.legacy.graph.io

import com.tinkerpop.blueprints.pgm.IndexableGraph
import org.pelagios.legacy.graph.io.readers.{GraphDatasetReader, GraphPlaceReader}
import org.pelagios.legacy.graph.io.readers.GraphAnnotationReader

/**
 * Provides Pelagios-specific Graph DB I/O (read) features.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class PelagiosGraphReader[T <: IndexableGraph](val graph: T) 
  extends PelagiosGraphIOBase 
  with GraphDatasetReader with GraphPlaceReader with GraphAnnotationReader {
  
}