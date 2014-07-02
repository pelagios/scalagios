package org.pelagios.rdf.parser

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.pelagios.Scalagios
import java.io.FileInputStream

@RunWith(classOf[JUnitRunner])
class VoIDParserTest extends FunSuite with UsesTestData {
  
  private val TEST_FILE = getFile("pelagios3.void.ttl")
  
  test("Gazetteer Dump Import") {
    println("Loading test VoID from " + TEST_FILE.getAbsolutePath)
    
    val is = new FileInputStream(TEST_FILE)
    val datasets = Scalagios.readVoID(is, TEST_FILE.getName).toSeq
  
    assert(datasets.size == 1, "Wrong number of datasets")
    
    val dataset = datasets.head    
    assert(dataset.title == "Pelagios 3")
    assert(dataset.description.isDefined)
    assert(dataset.description.get == "Early Geospatial Documents annotated and/or made available as part of the Pelagios 3 research project, funded by the Andrew W. Mellon Foundation.")
    assert(dataset.license == "http://creativecommons.org/publicdomain/zero/1.0/")
    assert(dataset.isSubsetOf.isEmpty)
    assert(dataset.subsets.size == 1)
    
    val subset = dataset.subsets(0)    
    assert(subset.title == "Latin Tradition Documents")
    assert(subset.description.isDefined)
    assert(subset.description.get == "Early Geospatial Documents from the Latin Tradition.")
    assert(subset.isSubsetOf.isDefined)
    assert(subset.isSubsetOf.get == dataset)
    assert(subset.subsets.size == 0)
    
    is.close()
  }

}