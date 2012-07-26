package org.scalagios.graph.io.readers

import scala.collection.JavaConverters._
import org.scalagios.graph.io.PelagiosGraphIOBase
import org.scalagios.api.{Dataset, GeoAnnotation, Place}
import org.scalagios.graph.Constants._
import org.scalagios.graph.VertexExtensions._
import org.scalagios.graph.{DatasetVertex, GeoAnnotationVertex, PlaceVertex}
import org.scalagios.graph.exception.GraphIntegrityException

trait GraphAnnotationReader extends PelagiosGraphIOBase {
  
  def getAnnotationsForTarget(uri: String): List[GeoAnnotation] =
    annotationIndex.get(ANNOTATION_TARGET_URI, uri).iterator.asScala.toList.map(new GeoAnnotationVertex(_))

  def getReferencedPlace(annotation: GeoAnnotation): Place = {
    val place = annotation.asInstanceOf[GeoAnnotationVertex].vertex.getFirstOutNeighbour(RELATION_HASBODY)
    if (place.isDefined)
      new PlaceVertex(place.get)
    else
      throw new GraphIntegrityException("Graph corrupt: annotation " + annotation.uri + " disconnected from body")
  }
  
  def getParentDataset(annotation: GeoAnnotation): Dataset =  {
    val dataset = annotation.asInstanceOf[GeoAnnotationVertex].vertex.getFirstInNeighbour(RELATION_CONTAINS)
    if (dataset.isDefined)
      new DatasetVertex(dataset.get)
    else
      throw new GraphIntegrityException("Graph corrupt: annotation " + annotation.uri + " disconnected from dataset")
  }
  
}