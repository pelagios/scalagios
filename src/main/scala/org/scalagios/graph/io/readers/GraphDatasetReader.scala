package org.scalagios.graph.io.readers

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import com.tinkerpop.blueprints.pgm.Vertex
import org.scalagios.api.{Dataset, Place}
import org.scalagios.graph.Constants._
import org.scalagios.graph.VertexExtensions._
import org.scalagios.graph.{DatasetVertex, PlaceVertex}
import org.scalagios.graph.io.PelagiosGraphIOBase
import scala.collection.mutable.Map
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import org.scalagios.graph.exception.GraphIntegrityException

trait GraphDatasetReader extends PelagiosGraphIOBase {

  def getDatasets(): List[Dataset] =
    datasetIndex.get(DATASET_URI, VIRTUAL_ROOT_URI).iterator.asScala.map(v => new DatasetVertex(v)).toList
  
  def getDataset(uri: String): Option[Dataset] = {
    val idxHits = datasetIndex.get(DATASET_URI, uri)
    if (idxHits.hasNext()) Some(new DatasetVertex(idxHits.next()))
    else None    
  }
  
  def findDatasetByHash(hash: String): Option[Dataset] = {
    val idxHits = datasetIndex.get(DATASET_HASH, hash)
    if (idxHits.hasNext()) Some(new DatasetVertex(idxHits.next()))
    else None       
  }

  def getReferencedPlaces(dataset: Dataset): Iterable[(Place, Int)] = {
    val places = Map.empty[Place, Int]
    _referencedPlacesRecursive(dataset, places)
    places.toIterable
  }
  
  def getConvexHull(dataset: Dataset): Option[Geometry] = {
    val wkt = dataset.asInstanceOf[DatasetVertex].vertex.getPropertyAsString(DATASET_CONVEX_HULL)
    if (wkt.isDefined)
      Some(new WKTReader().read(wkt.get))
    else
      None
  }
  
  private def _referencedPlacesRecursive(dataset: Dataset, places: Map[Place, Int]): Unit = {
    dataset.asInstanceOf[DatasetVertex].vertex.getOutEdges(RELATION_REFERENCES).asScala.foreach(edge => {
      val place = new PlaceVertex(edge.getInVertex)
      val count = edge.getProperty(REL_PROPERTY_REFERENCECOUNT).toString.toInt
      places.put(place, places.get(place).getOrElse(0) + count)
    })
    
    dataset.subsets.foreach(_referencedPlacesRecursive(_, places))
  } 
  
  def getDatasetHierarchy(dataset: Dataset): List[Dataset] = {
    _traverseHierarchy(dataset.asInstanceOf[DatasetVertex].vertex, ListBuffer.empty[Dataset]).toList
  }
  
  private def _traverseHierarchy(dataset: Vertex, hierarchy: ListBuffer[Dataset]): ListBuffer[Dataset] = {
    dataset.getInNeighbour(RELATION_SUBSET) match {
      case Some(vertex) => {
        hierarchy.append(new DatasetVertex(vertex))
        _traverseHierarchy(vertex, hierarchy) 
      }
      case None => hierarchy
    }
  } 

}