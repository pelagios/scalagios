package org.pelagios.rdf.parser.gazetteer

import org.openrdf.model.vocabulary.{ RDF, RDFS }
import org.pelagios.api.gazetteer.Place
import org.pelagios.rdf.parser.{ ResourceCollector, ResourceStreamHandler }
import org.pelagios.rdf.vocab.{ LAWD, Pelagios, W3CGeo }

class PlaceStreamHandler(val onNextPlace: Place => Unit, lowMemoryMode: Boolean = false) extends ResourceStreamHandler(LAWD.Place) {

  private var placeCounter = 0
  
  override def pushToStream() = {
	// Get all PlaceRecord resources from the cache
    val placeResources = cache.values.filter(resource => {
      val types = resource.get(RDF.TYPE)
      types.contains(LAWD.Place)
    })
    
    placeResources.foreach(resource => {
      val nameURIs = resource.get(LAWD.hasName)
      val locationURIs = resource.get(W3CGeo.location)
      
	  val nameResources = nameURIs.map(uri => cache.get(uri.stringValue))
      val locationResources = locationURIs.map(uri => cache.get(uri.stringValue))

      // If all URIs listed as the place's hasName and hasLocation properties are in the cache,
      // we consider the place complete 
      val isComplete = 
        ((nameResources.filter(_.isDefined).size == nameURIs.size) &&
         (locationResources.filter(_.isDefined).size == locationURIs.size))
         
      if (isComplete) {
		// We notify the callback handler...
        val names = nameResources.map(_.get.getFirst(LAWD.primaryForm).map(ResourceCollector.toPlainLiteral(_)).get)
        val locations = locationResources.map(r => new LocationResource(r.get))
        onNextPlace(new PlaceResource(resource, names, locations))
        
        // ...clear the place (along with names and locations) from the cache...
        cache.remove(resource.uri)
        nameURIs.foreach(uri => cache.remove(uri.stringValue))
        locationURIs.foreach(uri => cache.remove(uri.stringValue))
        
        // ...and increment the counter
        placeCounter += 1
      }
	})
	
	// Dangerous - completely flush the cache after a push 
	if (lowMemoryMode)
	  cache.clear()
  }
  
  override def endRDF(): Unit = {
    super.endRDF()
    logger.info(placeCounter + " places")
  }
	
}
