package org.pelagios.index

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.pelagios.Scalagios
import java.io.File
import org.scalatest.BeforeAndAfter
import org.apache.commons.io.FileUtils

@RunWith(classOf[JUnitRunner])
class PlaceIndexTest extends FunSuite with BeforeAndAfter {

  val TEST_PLACES_FILE = "places-new.ttl"
  val TEST_INDEX_DIR = "test-idx"
  val TEST_QUERY = "linz"
    
  before {
    val indexDir = new File(TEST_INDEX_DIR)
    if (indexDir.exists)
      FileUtils.deleteDirectory(indexDir)
  }
  
  test("Gazetteer Dump Import") {
    print("Loading test data from file... ")
    val places = Scalagios.parseGazetteerFile(new File(TEST_PLACES_FILE))
    println(places.size + " places.")
    
    print("Writing index... ")
    val index = PlaceIndex.open(new File(TEST_INDEX_DIR))
    index.addPlaces(places)
    println("done.")
    
    println("Running test query: '" + TEST_QUERY + "'")
    index.query(TEST_QUERY).foreach(place => {
      println(place.uri + " - " + place.title.label)
    })
    
    println("Done.")
  }
  
  after {
    FileUtils.deleteDirectory(new File(TEST_INDEX_DIR))
  }
  
}