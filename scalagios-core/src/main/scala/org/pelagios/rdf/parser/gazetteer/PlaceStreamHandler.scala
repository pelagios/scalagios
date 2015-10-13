package org.pelagios.rdf.parser.gazetteer

import org.openrdf.model.vocabulary.{ RDF, RDFS }
import org.pelagios.api.gazetteer.{ Location, Place }
import org.pelagios.rdf.parser.{ ResourceCollector, ResourceStreamHandler }
import org.pelagios.rdf.vocab.{ LAWD, GeoSPARQL, W3CGeo }
import org.pelagios.rdf.vocab.FOAF

class PlaceStreamHandler(val onNextPlace: Place => Unit, lowMemoryMode: Boolean) extends ResourceStreamHandler(LAWD.Place) {

  private var placeCounter = 0
  
  override def pushToStream() = {
	  // Get all PlaceRecord resources from the cache
    val placeResources = cache.values.filter(resource => {
      val types = resource.get(RDF.TYPE)
      types.contains(LAWD.Place)
    })
    
    placeResources.foreach(resource => {
      val nameURIs = resource.get(LAWD.hasName)
      val locationURI = resource.getFirst(W3CGeo.location)
      val geometryURI = resource.getFirst(GeoSPARQL.hasGeometry)
      val imageURIs = resource.get(FOAF.depiction)
      
	    val nameResources = nameURIs.flatMap(uri => cache.get(uri.stringValue))
      val locationResource = locationURI.flatMap(uri => cache.get(uri.stringValue))
      val geometryResource = geometryURI.flatMap(uri => cache.get(uri.stringValue))
      val imageResources = imageURIs.flatMap(uri => cache.get(uri.stringValue))

      // If all name, image, location, geometry URIs in the are in the cache, we consider it complete 
      val isComplete = 
        ((nameResources.size == nameURIs.size) && (imageResources.size == imageURIs.size) &&
         (locationResource.size == locationURI.size) && (geometryResource.size == geometryURI.size))
         
      if (isComplete) {
		    // We notify the callback handler...
        val names = nameResources.map(_.getFirst(LAWD.primaryForm).map(ResourceCollector.toPlainLiteral(_)).get)
        val coordinate = locationResource.flatMap(PlaceResource.toCoordinate(_))
        val geometry = geometryResource.flatMap(PlaceResource.toGeometry(_))
        val images = imageResources.map(PlaceResource.toImage(_))
        
        val placeResource = new PlaceResource(resource, names, Location.create(coordinate, geometry), images)
        onNextPlace(placeResource)
        
        // ...clear the place (along with all linked resources) from the cache...
        cache.remove(resource.uri)
        nameURIs.foreach(uri => cache.remove(uri.stringValue))
        locationURI.map(uri => cache.remove(uri.stringValue))
        geometryURI.map(uri => cache.remove(uri.stringValue))
        imageURIs.foreach(uri => cache.remove(uri.stringValue))
        
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
