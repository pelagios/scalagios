package org.scalagios.rdf.parser

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.openrdf.rio.turtle.TurtleParserFactory
import java.io.{File, FileInputStream}

@RunWith(classOf[JUnitRunner])
class VoIDParserTest extends FunSuite { 

  val SAMPLE_RDF = "src/test/resources/gap-void-sample.ttl"
    
  test("Pelagios VoID Import") {
    println("Starting import of sample VoID file")
    val startTime = System.currentTimeMillis
    
    val parser = new TurtleParserFactory().getParser()
    val datasetBuilder = new DatasetBuilder
    parser.setRDFHandler(datasetBuilder)
    parser.parse(new FileInputStream(new File(SAMPLE_RDF)), "http://googleancientplaces.wordpress.com/")
    
    println("Import complete. Took " + (System.currentTimeMillis - startTime) + " milliseconds")
    println(datasetBuilder.triplesTotal + " triples total in file")
    println(datasetBuilder.triplesProcessed + " triples processed during import")
    println(datasetBuilder.datasetsTotal + " datasets in VoID description")
    
    // TODO asserts!
    
    datasetBuilder.getDatasets.foreach(dataset => assert(dataset.isValid))
  }
  
}