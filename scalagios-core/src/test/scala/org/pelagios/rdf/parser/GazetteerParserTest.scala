package org.pelagios.rdf.parser

import java.io.File
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.pelagios.Scalagios
import java.io.FileInputStream
import org.openrdf.rio.RDFFormat

@RunWith(classOf[JUnitRunner])
class GazetteerParserTest extends FunSuite with UsesTestData {
  
  val TEST_FILE = getFile("../test-data/test-places-pleiades.ttl")
  
  test("Gazetteer Dump Import") {
    val is = new FileInputStream(TEST_FILE)
    val places = Scalagios.readPlaces(is, Scalagios.TURTLE).toList
    
    assert(places.size == 483, "invalid number of places")
    places.foreach(place => {
      assert(place.label != null, "title is null")
      assert(place.uri.startsWith("http://pleiades.stoa.org/places/") ||
             place.uri.startsWith("http://atlantides.org/capgrids/"), "invalid place URI - " + place.uri)
    })
    
    val placesWithLocations = places.filter(_.location.isDefined)
    assert(placesWithLocations.size == 377, "invalid number of places with locations (" + placesWithLocations.size + ")")
    
    val placesWithNames = places.filter(_.names.size > 0)
    assert(placesWithNames.size == 449, "invalid number of places with names")
    
    val placesWithDepiction = places.filter(_.depictions.size > 0)
    assert(placesWithDepiction.size == 1, "invalid number of places with depictions")
    
    is.close()
  }
  
}