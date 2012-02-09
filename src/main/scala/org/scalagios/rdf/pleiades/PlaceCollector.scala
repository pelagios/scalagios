package org.scalagios.rdf.pleiades

import scala.collection.mutable.HashMap
import org.openrdf.rio.helpers.RDFHandlerBase
import org.openrdf.model.Statement
import org.openrdf.model.vocabulary.RDFS
import org.scalagios.model.Place
import org.scalagios.model.impl.PlaceImpl

import org.scalagios.vocab.{SKOS, W3CGeo, OSSpatial, OSGeo}

class PlaceCollector extends RDFHandlerBase with ParseStats {
  
  private val placesBuffer = new HashMap[String, PlaceImpl]
  
  def getPlaces = placesBuffer.values
  
  def getPlace(uri: String) = placesBuffer.get(uri)
  
  override def handleStatement(statement: Statement) : Unit = {
    triplesTotal += 1
    
    var subj = statement.getSubject().stringValue()
    val obj = statement.getObject().stringValue()   
    
    // Remove one step of indirection that exists in the dump file
    if (subj.endsWith("-extent"))
        subj = subj.substring(0, subj.length() - 7)    
                
    val place = getOrCreate(subj);

    statement.getPredicate() match {
      case RDFS.LABEL => place.label = obj
      case RDFS.COMMENT => place.comment = obj
      case SKOS.ALT_LABEL => place.addAltLabel(obj)
      case W3CGeo.LAT => place.lat = obj.toDouble
      case W3CGeo.LONG => place.lon = obj.toDouble
      case OSSpatial.WITHIN => place.within = obj
      case OSGeo.asWKT => place.geometryWKT = obj
      case _ => triplesSkipped += 1
    }
  }
  
  def placesTotal = placesBuffer.size

  private def getOrCreate(uri: String): PlaceImpl = {   
    placesBuffer.get(uri) match {
      case Some(place) => place
      case None => {
        val place = new PlaceImpl(uri)
        placesBuffer.put(uri, place)
        place
      }
    }
  }

}