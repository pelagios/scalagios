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
  private val DATA_DARE = "../test-data/dare-20140324.ttl.gz"
  private val INDEX_DIR = "tmp-index"
    
  before {
    FileUtils.deleteDirectory(new File(INDEX_DIR))
  }
    
  test("Basic Index Operation") {
    println("### Setting up the index ###")
    println("Initializing index")
    val index = PlaceIndex.open(INDEX_DIR)

    println("Loading DARE data")
    val dare = Scalagios.readPlaces(new GZIPInputStream(new FileInputStream(DATA_DARE)), "http://imperium.ahlfeldt.se/", RDFFormat.TURTLE)
    println("Inserting " + dare.size + " places (" + dare.flatMap(_.names).size + " names) into index")
    index.addPlaces(dare)
    
    println("Loading Pleiades data")
    val pleiades = Scalagios.readPlaces(new GZIPInputStream(new FileInputStream(DATA_PLEIADES)), "http://pleiades.stoa.org/", RDFFormat.TURTLE)
    println("Inserting " + pleiades.size + " places (" + pleiades.flatMap(_.names).size + " names) into index")
    index.addPlaces(pleiades)

    println()
    println("### Testing retrieval by URI ###")
    println("Athens in DARE (expecting 1)")
    val athensDARE = index.findByURI("http://www.imperium.ahlfeldt.se/places/10975")
    assert(athensDARE.isDefined)
    assert(athensDARE.get.uri.startsWith("http://www.imperium.ahlfeldt"))
    println(athensDARE.get.title + " (" + athensDARE.get.names.map(_.label).mkString(", ") + ")")

    println("Athens in Pleiades (expecting 1)")
    val athensPleiades = index.findByURI("http://pleiades.stoa.org/places/579885")
    assert(athensPleiades.isDefined)
    assert(athensPleiades.get.uri.startsWith("http://pleiades.stoa.org"))
    println(athensPleiades.get.title + " (" + athensPleiades.get.names.map(_.label).mkString(", ") + ")")
    
    println("Expecting both Athens records to be in the same network")
    assert(athensDARE.get.seedURI.equals(athensPleiades.get.seedURI))
    
    println()
    println("### Testing simple query ###")
    println("Results for 'Athenae' (expecting 1 result from DARE)")
    val resultsAthens = index.query("Athenae").toSeq
    resultsAthens.foreach(place => println(place.title + " - " + place.uri + " (" + place.names.map(_.label).mkString(", ") + ")"))
    // assert(resultsAthens.size == 1, "Got wrong number of search results")
    // assert(resultsAthens(0).uri.startsWith("http://www.imperium.ahlfeldt.se"), "Result is not from DARE")
    
    // Athens: http://pleiades.stoa.org/places/579885
    // DARE Athens: http://www.imperium.ahlfeldt.se/places/10975
    
    println()
    println("### Testing network retrieval ###")
    val topHit = resultsAthens(0)
    println("Network for " + topHit.title + " (expecting 2 places)")
    val network = index.getNetwork(topHit)
    network.places.foreach(place => println(place.uri))
    
    println("Testing network conflation")
    println("Results for 'Roma'")
    val resultsRoma = index.query("roma").map(index.getNetwork(_)).toSeq
    val conflated = Network.conflateNetworks(resultsRoma)
    conflated.foreach(place=> {
      println(place.title + " (" + place.names.map(_.label).mkString(", ") + ")")
    })
    
    println("Done.")
  }
  
  after {
    FileUtils.deleteDirectory(new File(INDEX_DIR))
  }

}