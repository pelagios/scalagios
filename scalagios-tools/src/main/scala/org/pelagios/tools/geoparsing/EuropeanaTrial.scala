package org.pelagios.tools.geoparsing

import java.util.zip.GZIPInputStream
import java.io.FileInputStream
import scala.xml.XML

object EuropeanaTrial extends App {
    
  // Parse dataset XML
  val TEST_DATASET = "test-data/europeana-enrichment-trial.xml.gz"
  val xml = XML.load(new GZIPInputStream(new FileInputStream(TEST_DATASET)))

  (xml \\ "ProvidedCHO" \ "description").par.foreach(elem => {
    
    // Run selected fields through gazetteer index
    
    // Run selected fields through gazetteer index after NER
    
    // Write CSV
    val namedEntities = GeoParser.parse(elem.text)
    val toponyms = namedEntities.filter(_.category == "LOCATION") 
    toponyms.map(_.term).foreach(t => println(t))

  })

}