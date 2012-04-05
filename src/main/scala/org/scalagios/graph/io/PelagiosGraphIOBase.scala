package org.scalagios.graph.io

import org.scalagios.graph.Constants._
import com.tinkerpop.blueprints.pgm.{Vertex, IndexableGraph}
import java.net.URL
import com.tinkerpop.blueprints.pgm.Index
import com.tinkerpop.blueprints.pgm.impls.Parameter

/**
 * Abstract base class that holds functionality common across readers and writers.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
abstract class PelagiosGraphIOBase[T <: IndexableGraph](graph: T) {

  protected val placeIndex = getOrCreateIndex(INDEX_FOR_PLACES)  
  protected val datasetIndex = getOrCreateIndex(INDEX_FOR_DATASETS)
  protected val annotationIndex = getOrCreateIndex(INDEX_FOR_ANNOTATIONS)
  
  // Get (or lazily create) a named index  
  private def getOrCreateIndex(name: String): Index[Vertex] = {
    if (graph.getIndex(name, classOf[Vertex]) == null)
      graph.createManualIndex(name, classOf[Vertex], 
          new Parameter("to_lower_case", "true"),
          new Parameter("type", "fulltext"))
    else 
      graph.getIndex(name, classOf[Vertex])
  }
      
  protected def normalizeURL(s: String): String = {
    val url = new URL(s)
    var normalized = url.getProtocol + "://" + url.getHost + url.getPath 
    if (url.getRef != null) 
      if (!url.getRef.startsWith("this")) normalized += "#" + url.getRef
    normalized
  }
  
}