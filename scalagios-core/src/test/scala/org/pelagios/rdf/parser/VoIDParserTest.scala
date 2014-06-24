package org.pelagios.rdf.parser

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.pelagios.Scalagios
import java.io.FileInputStream

@RunWith(classOf[JUnitRunner])
class VoIDParserTest extends FunSuite with UsesTestData {
  
  private val TEST_FILE = getFile("pelagios.void.rdf")
  
  test("Gazetteer Dump Import") {
    println("Loading test VoID from " + TEST_FILE.getAbsolutePath)
    
    val is = new FileInputStream(TEST_FILE)
    val datasets = Scalagios.readVoID(is, TEST_FILE.getName).toSeq
    assert(datasets.size == 1, "Wrong number of datasets")
    
    val dataset = datasets.head
    assert(dataset.title == "American Numismatic Society")
    assert(dataset.description.isDefined)
    assert(dataset.description.get == "The Greek, Roman, and Byzantine coins of the American Numismatic Society.")
    assert(dataset.license == "http://opendatacommons.org/licenses/odbl/")
    assert(dataset.subjects.size == 1)
    assert(dataset.subjects.head == "http://dbpedia.org/resource/Annotation")
    assert(dataset.datadumps.size == 1)
    assert(dataset.datadumps.head == "http://numismatics.org/pelagios.rdf")
    
    is.close()
  }

}