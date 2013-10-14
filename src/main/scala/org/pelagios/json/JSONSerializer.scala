package org.pelagios.json

import org.pelagios.api._
import net.liftweb.json._

object JSONSerializer {
  
  import net.liftweb.json.JsonDSL._
    
  private def toFeatureCollection(locations: Seq[Location]): Option[JObject] = {
    if (locations.size > 0) {
      val features = locations.map(location => {  
        ("type" -> "Feature") ~ 
        ("geometry" -> (parse(location.geoJSON)))
      })
    
      Some(("type" -> "FeatureCollection") ~ ("features" -> features))
    } else {
      None
    }
  }
  
  def toJSON(places: Seq[Place], prettyPrint: Boolean = false): String = {
    val fCollections = places.map(place => {
      val names = place.names.map(name => name.labels ++ name.altLabels).flatten.map(_.label)
      ("source" -> place.uri) ~
      ("title" -> place.title) ~
      ("names" -> names) ~
      ("geometry" -> toFeatureCollection(place.locations)) 
    })
    
    if (prettyPrint)
      pretty(render(fCollections))
    else
      compact(render(fCollections))
  }

}