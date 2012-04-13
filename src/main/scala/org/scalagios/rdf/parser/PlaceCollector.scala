package org.scalagios.rdf.parser

import scala.collection.mutable.HashMap
import org.openrdf.rio.helpers.RDFHandlerBase
import org.openrdf.model.Statement
import org.openrdf.model.vocabulary.RDFS
import org.scalagios.api.{Place, DefaultPlace}
import org.scalagios.rdf.vocab.{SKOS, DCTerms, W3CGeo, OSSpatial, OSGeo}

/**
 * Analogous to the OpenRDF <em>StatementCollector</em>, this RDFHandler
 * implementation collects Places from a Pleiades RDF dump into a List.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class PlaceCollector extends RDFHandlerBase with HasStatistics {
  
  private[parser] val placesBuffer = new HashMap[String, DefaultPlace]
  
  def placesTotal = placesBuffer.size
  def getPlaces = placesBuffer.values
  
  override def handleStatement(statement: Statement) : Unit = {
    triplesTotal += 1
    
    var subj = statement.getSubject().stringValue()
    val obj = statement.getObject().stringValue()   
    
    // Remove one step of indirection that exists in the dump file
    if (subj.endsWith("-extent"))
        subj = subj.substring(0, subj.length() - 7)    
                
    val place = getOrCreate(subj);

    statement.getPredicate() match {
      case RDFS.LABEL => place.label = Some(obj)
      case RDFS.COMMENT => place.comment = Some(obj)
      case SKOS.altLabel => place.addAltLabel(obj)
      case DCTerms.coverage => place.coverage = Some(obj)
      case W3CGeo.lat => place.lat = obj.toDouble
      case W3CGeo.long => place.lon = obj.toDouble
      case OSSpatial.within => place.within = Some(getOrCreate(obj))
      case OSGeo.asWKT => place.geometryWKT = Some(obj)
      case _ => triplesSkipped += 1
    }
  }

  private def getOrCreate(uri: String): DefaultPlace = {   
    placesBuffer.get(uri) match {
      case Some(place) => place
      case None => {
        val place = new DefaultPlace(uri)
        placesBuffer.put(uri, place)
        place
      }
    }
  }

}