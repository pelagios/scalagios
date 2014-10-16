package org.pelagios.gazetteer

import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import org.junit.runner.RunWith
import org.pelagios.Scalagios
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfter
import org.apache.commons.io.FileUtils
import java.io.File

@RunWith(classOf[JUnitRunner])
class PlaceIndexTest extends FunSuite with BeforeAndAfter {
  
  private val DATA_PLEIADES = "test-data/pleiades-20120826-migrated.ttl.gz"
  private val DATA_DARE = "test-data/dare-20140324.ttl.gz"
  private val INDEX_DIR = "tmp-index"
    
  before {
    FileUtils.deleteDirectory(new File(INDEX_DIR))
  }
    
  test("Basic Index Operation") {
    println("### Setting up the index ###")
    println("Initializing index")
    val index = PlaceIndex.open(INDEX_DIR)

    println("Loading DARE data")
    val dareIs = new GZIPInputStream(new FileInputStream(DATA_DARE))
    val dare = Scalagios.readPlaces(dareIs, Scalagios.TURTLE)
    println("Inserting " + dare.size + " places (" + dare.flatMap(_.names).size + " names) into index")
    index.addPlaces(dare.toIterable)
    
    println("Loading Pleiades data")
    val pleiadesIs = new GZIPInputStream(new FileInputStream(DATA_PLEIADES))
    val pleiades = Scalagios.readPlaces(pleiadesIs, Scalagios.TURTLE)
    println("Inserting " + pleiades.size + " places (" + pleiades.flatMap(_.names).size + " names) into index")
    index.addPlaces(pleiades.toIterable)

    println()
    println("### Testing retrieval by URI ###")
    print("Athenae in DARE (expecting 1)")
    val athensDARE = index.findByURI("http://www.imperium.ahlfeldt.se/places/10975")
    assert(athensDARE.isDefined)
    assert(athensDARE.get.uri.startsWith("http://www.imperium.ahlfeldt"))
    println(" - ok")
    // println(athensDARE.get.title + " (" + athensDARE.get.names.map(_.label).mkString(", ") + ")")

    print("Athenae in Pleiades (expecting 1)")
    val athensPleiades = index.findByURI("http://pleiades.stoa.org/places/579885")
    assert(athensPleiades.isDefined)
    assert(athensPleiades.get.uri.startsWith("http://pleiades.stoa.org"))
    println(" - ok")
    // println(athensPleiades.get.title + " (" + athensPleiades.get.names.map(_.label).mkString(", ") + ")")
    
    print("Expecting both Athenae records to be in the same network")
    assert(athensDARE.get.seedURI.equals(athensPleiades.get.seedURI))
    println(" - ok")
    
    println()
    println("### Testing exact name match query ###")
    print("Results for 'Athenae' (expecting 2 result from DARE and 2 from Pleiades)")
    val resultsAthens = index.query("Athenae").toSeq
    assert(resultsAthens.size == 4, "Got wrong number of search results")
    assert(resultsAthens.filter(_.uri.startsWith("http://www.imperium.ahlfeldt.se")).size == 2, "Wrong number of result from DARE")
    assert(resultsAthens.filter(_.uri.startsWith("http://pleiades.stoa.org")).size == 2, "Wrong number of result from Pleiades")
    println(" - ok")    
    // resultsAthens.foreach(place => println(place.title + " - " + place.uri + " (" + place.names.map(_.label).mkString(", ") + ")"))
    
    println()
    val topHit = resultsAthens(0)
    println("### Testing network retrieval ###")
    print("Network for " + topHit.label + " (expecting 2 places)")
    val network = index.getNetwork(topHit)
    assert(network.places.size == 2, "Wrong number of places in network")
    assert(network.places.filter(_.uri.startsWith("http://www.imperium.ahlfeldt.se")).size == 1, "Wrong number of result from DARE")
    assert(network.places.filter(_.uri.startsWith("http://pleiades.stoa.org")).size == 1, "Wrong number of result from Pleiades")
    println(" - ok")
    // network.places.foreach(place => println(place.uri))
    
    println()
    println("### Testing network conflation ###")
    print("Getting network for 'Roma' (expecting 1 network with 2 places)")
    val resultsRoma = index.findByURI("http://www.imperium.ahlfeldt.se/places/1438").map(index.getNetwork(_)).toSeq
    assert(resultsRoma.size == 1)
    assert(resultsRoma(0).places.size == 2)
    val conflated = Network.conflateNetworks(resultsRoma)
    assert(conflated.size == 1)
    println(" - ok")
    
    dareIs.close()
    pleiadesIs.close()
    println("Done.")
  }
  
  after {
    FileUtils.deleteDirectory(new File(INDEX_DIR))
  }

}