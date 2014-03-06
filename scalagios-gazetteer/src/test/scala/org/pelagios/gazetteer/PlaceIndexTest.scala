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
    println("Initializing index")
    val index = PlaceIndex.open(INDEX_DIR)

    println("Loading Pleiades data")
    val pleiades = Scalagios.readPlaces(new GZIPInputStream(new FileInputStream(DATA_PLEIADES)), "http://pleiades.stoa.org/", RDFFormat.TURTLE)
    println("Inserting " + pleiades.size + " places (" + pleiades.flatMap(_.names).size + " names) into index")
    index.addPlaces(pleiades)
    
    println("Loading DARE data")
    val dare = Scalagios.readPlaces(new GZIPInputStream(new FileInputStream(DATA_DARE)), "http://imperium.ahlfeldt.se/", RDFFormat.TURTLE)
    println("Inserting " + dare.size + " places (" + dare.flatMap(_.names).size + " names) into index")
    index.addPlaces(dare)

    println("Test query...")
    println("Results for 'Athína'")
    val resultsTitle = index.query("Athína").toSeq
    resultsTitle.foreach(place => println(place.title + " - " + place.uri + " (" + place.names.map(_.label).mkString(", ")))

    println("Results for 'Αθήνα'")
    val resultsNames = index.query("Αθήνα").toSeq
    resultsNames.foreach(place => println(place.title + " - " + place.uri + " (" + place.names.map(_.label).mkString(", ")))
    
    val topHit = resultsTitle(0)
    println("Network for " + topHit.title + " (" + topHit.uri + ")")
    val network = index.getNetwork(topHit)
    network.places.foreach(place => println(place.uri))
    
    println("Done.")
  }
  
  after {
    FileUtils.deleteDirectory(new File(INDEX_DIR))
  }

}