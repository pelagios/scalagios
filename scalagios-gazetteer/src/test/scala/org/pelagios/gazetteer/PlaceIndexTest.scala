package org.pelagios.gazetteer

import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import org.junit.runner.RunWith
import org.openrdf.rio.RDFFormat
import org.pelagios.Scalagios
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfter
import org.apache.commons.io.FileUtils
import java.io.File

@RunWith(classOf[JUnitRunner])
class PlaceIndexTest extends FunSuite with BeforeAndAfter {
  
  private val DATA_PLEIADES = "../test-data/pleiades-20120826-migrated.ttl.gz"
  private val DATA_DARE = "../test-data/dare-20131210.ttl.gz"
  private val INDEX_DIR = "tmp-index"
    
  before {
    FileUtils.deleteDirectory(new File(INDEX_DIR))
  }
    
  test("Basic Index Operation") {
    println("Initializing inded")
    val index = PlaceIndex.open(INDEX_DIR)

    println("Loading Pleiades data")
    val pleiades = Scalagios.readPlaces(new GZIPInputStream(new FileInputStream(DATA_PLEIADES)), "http://pleiades.stoa.org/", RDFFormat.TURTLE)
    println("Inserting Pleiades into index")
    index.addPlaces(pleiades)
    
    println("Loading DARE data")
    val dare = Scalagios.readPlaces(new GZIPInputStream(new FileInputStream(DATA_DARE)), "http://imperium.ahlfeldt.se/", RDFFormat.TURTLE)
    println("Inserting DARE into index") 
    index.addPlaces(dare)
    
    println("Test query...")
    val results = index.query("vienna").toSeq
    results.foreach(place => println(place.title))

    val topHit = results(0)
    println("Network for " + topHit.title + " (" + topHit.uri + ")")
    val network = index.getNetwork(topHit)
    network.places.foreach(place => println(place.uri))
    
    println("Done.")
  }
  
  after {
    FileUtils.deleteDirectory(new File(INDEX_DIR))
  }

}