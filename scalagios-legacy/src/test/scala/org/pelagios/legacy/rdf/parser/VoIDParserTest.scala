package org.pelagios.legacy.rdf.parser

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.openrdf.rio.turtle.TurtleParserFactory
import java.io.{File, FileInputStream}
import org.openrdf.rio.RDFParserRegistry
import org.pelagios.legacy.api.Dataset

@RunWith(classOf[JUnitRunner])
class VoIDParserTest extends FunSuite { 

  val SAMPLE_RDF = "src/test/resources/legacy/test-void-gap.ttl"
  val ANNOTATION_BASEURI = "http://gap.alexandriaarchive.org/bookdata/GAPtriples/"
    
  test("Pelagios VoID Import") {    
    println("Starting import of sample VoID file")
    val startTime = System.currentTimeMillis
    
    val parser = new TurtleParserFactory().getParser()
    val datasetBuilder = new DatasetCollector()
    parser.setRDFHandler(datasetBuilder)
    parser.parse(new FileInputStream(new File(SAMPLE_RDF)), ANNOTATION_BASEURI)
    
    println("Import complete. Took " + (System.currentTimeMillis - startTime) + " milliseconds")
    println(datasetBuilder.triplesTotal + " triples total in file")
    println(datasetBuilder.triplesProcessed + " triples processed during import")
    println(datasetBuilder.datasetsTotal + " datasets in VoID description")
    
    // TODO test should verify with asserts, not just print to screen
    datasetBuilder.getRootDatasets.foreach(dataset => printToScreen(dataset, ""))
  }
  
  private def printToScreen(dataset: Dataset, indent: String): Unit = {
    println(indent + dataset.title)
    dataset.subsets.toList.sortBy(_.uri).foreach(subset => {
      printToScreen(subset, "   " + indent)
    })
  }
  
}