package org.pelagios.rdf.serializer

import com.vividsolutions.jts.io.WKTWriter
import java.io.{ File, PrintWriter }
import org.pelagios.api.gazetteer.{ Location, Place } 
import org.openrdf.rio.RDFFormat
import org.pelagios.Scalagios
import org.pelagios.rdf.vocab.PelagiosPlaceCategories

object GazetteerSerializer {

  def writeToFile(places: Iterable[Place], file: String, format: String) = {
    // TODO support other formats beyond Turtle
    if (!format.equalsIgnoreCase(Scalagios.TURTLE))
      throw new UnsupportedOperationException
      
    val f = new File(file)
    if (!f.exists)
      f.createNewFile
   
    val writer = new PrintWriter(f)
      
    // Write header
    writer.println("@prefix dcterms: <http://purl.org/dc/terms/> .")
    writer.println("@prefix geo:<http://www.w3.org/2003/01/geo/wgs84_pos#> .")
    writer.println("@prefix lawd: <http://lawd.info/ontology/> .")
    writer.println("@prefix osgeo: <http://data.ordnancesurvey.co.uk/ontology/geometry/> .")
    writer.println("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .")
    writer.println()

    val wktWriter = new WKTWriter()
    
    places.foreach(place => {
      writer.println("<" + place.uri + "> a lawd:Place;")
      writer.println("  rdfs:label \"" + place.label.replaceAll("\\\"", "\\\\\"") + "\" ;")
      place.descriptions.foreach(d => 
        writer.println("  dcterms:description \"" + d.chars.replaceAll("\\\"", "\\\\\"") + d.lang.map("@" + _).getOrElse("") + "\" ;"))
     
      if (place.category.isDefined)
        writer.println("  dcterms:type <" + PelagiosPlaceCategories.fromCategory(place.category.get) + "> ;")

      place.subjects.foreach(s => 
        writer.println("  dcterms:subject <" + s + "> ;"))

      place.names.foreach(name => {
        val label = name.chars.trim
        if (!label.isEmpty)
          writer.println("  lawd:hasName [ lawd:primaryForm \"" + label + name.lang.map("@" + _).getOrElse("") + "\" ] ;")
      })
      
      if (place.location.isDefined)
        writer.println("  geo:location [ geo:lat " + place.location.get.pointLocation.y + " ; geo:long " + place.location.get.pointLocation.x + " ] ;")
      
      writer.println("  .\n")
    })
    
    writer.flush
    writer.close    
  }
  
}
