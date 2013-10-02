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

  val TEST_PLACES_FILE = "src/test/resources/one-place.ttl"
  val TEST_INDEX_DIR = "test-idx"
    
  before {
    val indexDir = new File(TEST_INDEX_DIR)
    if (indexDir.exists)
      FileUtils.deleteDirectory(indexDir)
  }
  
  test("Gazetteer Dump Import") {
    println("Loading test data from file")
    val places = Scalagios.parseGazetteerFile(new File(TEST_PLACES_FILE))
    
    println("Writing index")
    val index = PlaceIndex.open(new File(TEST_INDEX_DIR))
    index.addPlaces(places)
    
    println("Running test query")
    val results = index.query("athens")
    
    results.foreach(place => {
      println(place.uri + " - " + place.title.label)
    })
  }
  
  after {
    FileUtils.deleteDirectory(new File(TEST_INDEX_DIR))
  }
  
}