package org.scalagios.graph.io

import com.tinkerpop.blueprints.pgm.IndexableGraph
import org.scalagios.graph.Constants._
import com.tinkerpop.blueprints.pgm.{Vertex, IndexableGraph}
import java.net.URL

/**
 * Abstract base class that holds functionality common across readers and writers.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
abstract class PelagiosGraphIOBase[T <: IndexableGraph](graph: T) {

  // Get (or lazily create) the place index
  protected val placeIndex = 
    if (graph.getIndex(INDEX_FOR_PLACES, classOf[Vertex]) == null)
      graph.createManualIndex(INDEX_FOR_PLACES, classOf[Vertex])
    else 
      graph.getIndex(INDEX_FOR_PLACES, classOf[Vertex])
    
  // Get (or lazily create) the annotation index
  protected val annotationIndex = 
    if (graph.getIndex(INDEX_FOR_ANNOTATIONS, classOf[Vertex]) == null)
      graph.createManualIndex(INDEX_FOR_ANNOTATIONS, classOf[Vertex])
    else
      graph.getIndex(INDEX_FOR_ANNOTATIONS, classOf[Vertex])  
      
  protected def normalizeURL(s: String): String = {
    val url = new URL(s)
    url.getProtocol + "://" + url.getHost + url.getPath
  }
  
}