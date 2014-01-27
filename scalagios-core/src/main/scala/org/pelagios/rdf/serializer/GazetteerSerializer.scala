package org.pelagios.rdf.serializer

import com.vividsolutions.jts.io.WKTWriter
import java.io.{ File, PrintWriter }
import org.pelagios.api.Place
import org.openrdf.rio.RDFFormat
import org.pelagios.rdf.vocab.PelagiosPlaceCategories

object GazetteerSerializer {

  def writeToFile(places: Iterable[Place], file: String, format: RDFFormat) = {
    // TODO support other formats beyond Turtle
    if (format != RDFFormat.TURTLE)
      throw new UnsupportedOperationException
      
    val f = new File(file)
    if (!f.exists)
      f.createNewFile
   
    val writer = new PrintWriter(f)
      
    // Write header
    writer.println("@prefix dcterms: <http://purl.org/dc/terms/> .")   
    writer.println("@prefix osgeo: <http://data.ordnancesurvey.co.uk/ontology/geometry/> .")   
    writer.println("@prefix pelagios: <http://pelagios.github.io/vocab/terms#> .")
    writer.println("@prefix pleiades: <http://pleiades.stoa.org/places/vocab#> .\n")
    
    val wktWriter = new WKTWriter()
    
    places.foreach(place => {
      writer.println("<" + place.uri + "> a pelagios:PlaceRecord ;")
      writer.println("  dcterms:title \"" + place.title.replaceAll("\\\"", "\\\\\"") + "\" ;")
      place.descriptions.foreach(d => 
        writer.println("  dcterms:description \"" + d.label.replaceAll("\\\"", "\\\\\"") + d.lang.map("@" + _).getOrElse("") + "\" ;"))
     
      if (place.category.isDefined)
        writer.println("  dcterms:type <" + PelagiosPlaceCategories.fromCategory(place.category.get) + "> ;")

      place.subjects.foreach(s => 
        writer.println("  dcterms:subject <" + s + "> ;"))

      place.names.foreach(n => {
        (n.labels ++ n.altLabels).foreach(l => {
          val label = l.label.trim
          if (!label.isEmpty)
            writer.println("  pleiades:hasName [ rdfs:label \"" + label + l.lang.map("@" + _).getOrElse("") + "\" ] ;")
        })
      })
      
      place.locations.foreach(l => {
        writer.println("  pleiades:hasLocation [ osgeo:asWKT \"" + wktWriter.write(l.geometry) + "\" ] ;")
      })
      
      writer.println("  .\n")
    })
    
    writer.flush
    writer.close    
  }
  
}