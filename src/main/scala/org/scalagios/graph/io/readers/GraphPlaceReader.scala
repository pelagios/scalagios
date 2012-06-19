package org.scalagios.graph.io.readers

import scala.collection.JavaConverters._
import org.scalagios.graph.Constants._
import org.scalagios.graph.{DatasetVertex, PlaceVertex}
import org.scalagios.graph.io.PelagiosGraphIOBase
import org.scalagios.api.{Dataset, Place}

trait GraphPlaceReader extends PelagiosGraphIOBase {

  def getPlaces(): Iterable[Place] = getVertices(PLACE_VERTEX).map(vertex => new PlaceVertex(vertex))
  
  def getPlace(uri: String): Option[Place] = {
    val idxHits = placeIndex.get(PLACE_URI, uri)
    
    if (idxHits.hasNext())
      Some(new PlaceVertex(idxHits.next()))
    else
      None
  }
  
  /**
   * Returns all datasets that reference the specified place
   */
  def getReferencingDatasets(placeUri: String): Iterable[(Dataset, Int)] = {
    val idxHits = placeIndex.get(PLACE_URI, placeUri)
    if (idxHits.hasNext())
      idxHits.next.getInEdges(RELATION_REFERENCES).asScala
        .map(edge => (new DatasetVertex(edge.getOutVertex) -> edge.getProperty(REL_PROPERTY_REFERENCECOUNT).toString.toInt))
    else
      Seq.empty[(Dataset, Int)]
  }
  
  /**
   * Returns all datasets that (i) reference the specified place and
   * (ii) are equal to or located below the specified dataset  
   */
  def getReferencingDatasets(placeUri: String, datasetUri: String): Iterable[(Dataset, Int)] =
    getReferencingDatasets(placeUri).filter{ case(dataset, count) => 
      dataset.uri.equals(datasetUri) || dataset.isChildOf(datasetUri) }
  
  // TODO Might be slow in a large DB! Although we'll probably use this rarely, we'll need a more scalable solution
  private[io] def getVertices(vertexType: String) = graph.getVertices().asScala.filter(_.getProperty(VERTEX_TYPE).equals(vertexType))
  
}