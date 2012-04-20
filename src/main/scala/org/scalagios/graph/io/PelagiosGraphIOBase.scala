package org.scalagios.graph.io

import java.net.URL

import com.tinkerpop.blueprints.pgm.{Vertex, IndexableGraph}
import com.tinkerpop.blueprints.pgm.Index
import com.tinkerpop.blueprints.pgm.impls.Parameter

import org.scalagios.graph.Constants._

private[io] trait PelagiosGraphIOBase {
  
  // Subclasses must provide a "graph" member
  val graph: IndexableGraph

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
    if (normalized.endsWith("/"))
      normalized = normalized.substring(0, normalized.length - 1)
    
    if (url.getRef != null) 
      if (!url.getRef.startsWith("this")) normalized += "#" + url.getRef
    normalized
  }
  
}