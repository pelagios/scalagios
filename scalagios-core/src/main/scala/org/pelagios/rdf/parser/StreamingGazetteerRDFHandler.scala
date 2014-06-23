package org.pelagios.rdf.parser

import scala.collection.mutable.HashMap
import org.openrdf.model.Statement
import org.openrdf.model.vocabulary.{ RDF, RDFS }
import org.openrdf.rio.helpers.RDFHandlerBase
import org.pelagios.api.gazetteer.Place
import org.pelagios.rdf.vocab.{ Pelagios, PleiadesPlaces }
import org.slf4j.LoggerFactory

class StreamingGazetteerRDFHandler(val handlePlace: Place => Unit) extends RDFHandlerBase {
	
  private val logger = LoggerFactory.getLogger(classOf[StreamingGazetteerRDFHandler])
  
  private val startTime = System.currentTimeMillis

  private val PROGRESS_LOG_STEP = 50000
  
  private val cache = new HashMap[String, Resource]

  private var tripleCounter = 0
  
  private def exportPlaces() = {
	// Step 1 - get all PlaceRecord resources
    val placeResources = cache.values.filter(resource => {
      val types = resource.get(RDF.TYPE)
      types.contains(Pelagios.PlaceRecord)
    })
    
    placeResources.foreach(resource => {
      val nameURIs = resource.get(PleiadesPlaces.hasName)
      val locationURIs = resource.get(PleiadesPlaces.hasLocation)
      
	  val nameResources = nameURIs.map(uri => cache.get(uri.stringValue))
      val locationResources = locationURIs.map(uri => cache.get(uri.stringValue))

      // If all URIs listed as the place's hasName and hasLocation properties are in the cache,
      // we consider the place complete 
      val isComplete = 
        ((nameResources.filter(_.isDefined).size == nameURIs.size) &&
         (locationResources.filter(_.isDefined).size == locationURIs.size))
         
      if (isComplete) {
		// We notify the callback handler...
        val names = nameResources.map(_.get.getFirst(RDFS.LABEL).map(ResourceCollector.toLabel(_)).get)
        val locations = locationResources.map(r => new LocationResource(r.get))
        handlePlace(new PlaceResource(resource, names, locations))
        
        // And clear the place - along with names and locations - from the cache
        cache.remove(resource.uri)
        nameURIs.foreach(uri => cache.remove(uri.stringValue))
        locationURIs.foreach(uri => cache.remove(uri.stringValue))
      }
	})
  }

  override def handleStatement(s: Statement): Unit = {
    tripleCounter += 1

    val subj = s.getSubject.stringValue
    val pred = s.getPredicate
    val obj = s.getObject
    
    // In case of subject -> rdf:type -> pelagios:PlaceRecord purge the cache
    if ((pred == RDF.TYPE) && (obj == Pelagios.PlaceRecord))
      exportPlaces()
    
    // Keep parsing...
    val resource = cache.getOrElse(subj, new Resource(subj))
    resource.properties.append((pred, obj))
    cache.put(subj, resource)
    
    if (tripleCounter % PROGRESS_LOG_STEP == 0)
      logger.info("Imported " + tripleCounter + " triples to staging index")
  }
  
  override def endRDF(): Unit = {
	cache.clear()
    logger.info("File parsing complete (" + tripleCounter + " triples)")
    logger.info("Took " + (System.currentTimeMillis - startTime) + "ms")
  }
	
}
