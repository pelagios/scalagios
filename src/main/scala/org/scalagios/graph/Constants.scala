package org.scalagios.graph

/**
 * String constants used as field labels in the Graph DB (index, nodes, relations, etc.)
 * 
 * @author Rainer Simon<rainer.simon@ait.ac.at>
 */
object Constants {
  
  /**
   *  Index names used by Neo4j
   */
  val INDEX_FOR_PLACES = "place-index"
  val INDEX_FOR_DATASETS = "dataset-index"
  val INDEX_FOR_ANNOTATIONS = "annotation-index"
  
  /**
   * 'Virtual URI' used to make root datasets accessible via the index
   */
  val VIRTUAL_ROOT_URI = "scalagios://datasets/"
    
  /**
   * Vertex types
   */
  val VERTEX_TYPE = "type"
  val PLACE_VERTEX = "place"
  val DATASET_VERTEX = "dataset"
  val ANNOTATION_VERTEX = "geoannotation"
  val ANNOTATION_TARGET_VERTEX = "geoannotation_target"

  /**
   * Place vertex properties
   */
  val PLACE_URI = "uri"
  val PLACE_LABEL = "label"
  val PLACE_COMMENT = "comment"
  val PLACE_ALTLABELS = "altLabels"
  val PLACE_COVERAGE = "coverage"
  val PLACE_LON = "lon"
  val PLACE_LAT = "lat"
  val PLACE_WITHIN = "within"
  val PLACE_GEOMETRY = "geometry"
    
  /**
   * Dataset vertex properties
   */
  val DATASET_URI = "uri"
  val DATASET_TITLE = "title"
  val DATASET_DESCRIPTION = "description"
  val DATASET_LICENSE = "license"
  val DATASET_HOMEPAGE = "homepage"
  val DATASET_HASH = "hashCode"
    
  /**
   * GeoAnnotation vertex properties
   */
  val ANNOTATION_URI = "uri"
  val ANNOTATION_TITLE = "title"
  val ANNOTATION_BODY = "body"
  val ANNOTATION_TARGET = "target"
    
  /**
   * GeoAnnotationTarget vertex properties
   */
  val ANNOTATION_TARGET_URI = "uri"
  val ANNOTATION_TARGET_TITLE = "title"
    
  /**
   * Graph relationship type 'within': Place -> Place
   */
  val RELATION_WITHIN = "within"
    
  /**
   * Graph relationship type 'subset': Dataset -> Dataset
   */
  val RELATION_SUBSET = "subset"
    
  /**
   * Graph relationship type 'hasBody': GeoAnnotation -> Place
   */
  val RELATION_HASBODY = "hasBody"
    
  /**
   * Graph relationship type 'hasTarget': GeoAnnotation -> GeoAnnotationTarget
   */
  val RELATION_HASTARGET = "hasTarget"

}