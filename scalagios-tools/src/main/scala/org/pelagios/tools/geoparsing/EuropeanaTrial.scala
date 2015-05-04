package org.pelagios.tools.geoparsing

import java.util.zip.GZIPInputStream
import java.io.FileInputStream
import scala.xml.XML
import scala.xml.Elem
import org.pelagios.tools.georesolution.GeoResolver
import org.pelagios.gazetteer.PlaceIndex
import java.io.PrintWriter

object EuropeanaTrial extends App {
  
  val TEST_DATASET = "test-data/europeana-enrichment-trial.xml.gz"
  
  val WIKIDATA_GAZETTEER = "test-data/22_oct_wiki_dump.ttl"
  
  private def initIndex(idx: PlaceIndex) = {    
    println("There is no gazetter index yet - just a second...")     
    println("Loading gazetteer data")
    idx.addPlaceStream(new FileInputStream(WIKIDATA_GAZETTEER), "22_oct_wiki_dump.ttl", true)
    println("Index complete")          
  }
  
  val startTime = System.currentTimeMillis
  
  val xml = XML.load(new GZIPInputStream(new FileInputStream(TEST_DATASET)))
  
  val idx = PlaceIndex.open("index") 
  if (idx.isEmpty)
    initIndex(idx)
  
  val resolver = new GeoResolver(PlaceIndex.open("index") )
  val printWriter = new PrintWriter("europeana-enrichment.csv")

  (xml \\ "ProvidedCHO").foreach(node =>
    
    if (node.isInstanceOf[Elem]) {
      
      val rdfAbout = node.asInstanceOf[Elem].attributes.head.value.text
    
      val places = (node \ "_").par map (_ match {        
        case e: Elem => e.label match {
         
          // Run these fields through NER and then geo-resolution
          case "title" | "description" | "source" | "publisher" => {
            GeoParser.parse(e.text).filter(_.category == "LOCATION").map(_.term).foreach(toponym => {
              val place = resolver.matchToponym(toponym)
              if (place.isDefined)
                printWriter.write(rdfAbout + ";" + e.label + ";" + place.get.uri + ";" + place.get.label + ";ner;" + toponym + ";\n")
            }) 
          }
          
          // Run these fields JUST through geo-resolution
          case "spatial" | "currentLocation" | "subject" => {
            try {
              val place = resolver.matchToponym(e.text.replace("~", " ").replace("*", " ")) // Just minimal cleanup
              if (place.isDefined)
                printWriter.write(rdfAbout + ";" + e.label + ";" + place.get.uri + ";" + place.get.label + ";no_ner;" + e.text + ";\n")          
            } catch {
              case t: Throwable => println(t.getClass.toString() + ": " + t.getMessage)
            }
          }
          
          case _ =>
            
        }
      })
    
    }

  )
  
  printWriter.flush()
  printWriter.close()
  
  println("Done. Took " + (System.currentTimeMillis - startTime) / 1000 + "s.")
  
}