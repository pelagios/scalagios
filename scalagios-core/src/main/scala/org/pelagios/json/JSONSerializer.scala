package org.pelagios.json

import org.pelagios.api._
import net.liftweb.json._
import com.vividsolutions.jts.geom.Coordinate

/** GeoJSON-based serialization for places and annotations. 
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
object JSONSerializer {
  
  import net.liftweb.json.JsonDSL._
      
  private def placeToJSON(place: Place): JObject = {
    val names = place.names.map(name => name.labels ++ name.altLabels).flatten.map(_.label)
    
    val locations = if (place.locations.size > 0) {
      val features = place.locations.map(location => {  
        ("type" -> "Feature") ~ 
        ("geometry" -> (parse(location.geoJSON)))
      })
      Some(("type" -> "FeatureCollection") ~ ("features" -> features))
    } else {
      None
    }
    
    ("source" -> place.uri) ~
    ("title" -> place.title) ~
    ("names" -> names) ~
    ("geometry" -> locations) 
  }
  
  private def _serializeAnnotations(annotations: Seq[Annotation], 
                                   prettyPrint: Boolean = false, 
                                   geoResolutionFn: Option[String => Option[Place]] = None): String = {
    
    // First, filter out annotations that do not provide gazetteer URIs
    val annotationsWithPlaceReference = annotations.filter(_.place.size > 0)
    
    // We pick the first gazetteer URI in the list
    // The spec requires them to be identical as far as georesolution is concerned, anyway
    val data: Seq[(Annotation, Place)] = annotationsWithPlaceReference.map(annotation =>
      (annotation, geoResolutionFn.get(annotation.place(0))))
      .filter(_._2.isDefined) // We filter out all place we couldn't resolve
      .map(tuple => (tuple._1, tuple._2.get)) // And get rid of the 'Option' wrapper
      
    val json = data.map { case (annotation, place) => {
      ("transcription" -> annotation.transcription.map(_.chars)) ~
      ("place" -> placeToJSON(place))
    }}
      
    if (prettyPrint) pretty(render(json)) else compact(render(json))
  }
  
  def serializePlaces(places: Seq[Place], prettyPrint: Boolean = false): String = {
    val json = places.map(placeToJSON(_)) 
    if (prettyPrint) pretty(render(json)) else compact(render(json))
  }
  
  def serializeAnnotations(annotations: Seq[Annotation]): String = 
    _serializeAnnotations(annotations, false, None)
  
  def serializeAnnotations(annotations: Seq[Annotation], pretty: Boolean): String = 
    _serializeAnnotations(annotations, pretty, None)
  
  def serializeAnnotations(annotations: Seq[Annotation], pretty: Boolean, geoResolutionFn: String => Option[Place]): String = 
    _serializeAnnotations(annotations, pretty, Some(geoResolutionFn))

}