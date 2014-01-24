package org.pelagios.legacy

import java.io.{ File, FileInputStream }
import org.pelagios.Scalagios
import org.pelagios.legacy.api.{ GeoAnnotation, Place }
import org.pelagios.legacy.rdf.parser.{ AnnotationCollector, PlaceCollector}
import java.io.InputStream
import org.openrdf.rio.RDFFormat
import org.openrdf.rio.RDFParserRegistry
import org.openrdf.rio.RDFParser

/** Legacy import utilities. **/
object LegacyInterop {
  
  /** Parses a data dump in the old OAC-based Pelagios format into the legacy API.
    *
    * @param file the legacy dump file
    * @return a list of legacy [[org.pelagios.legacy.api.GeoAnnotation]] objects  
    */
  def parseOAC(file: File): Iterable[GeoAnnotation] = {
    val parser = Scalagios.getParser(file.getName)
    val annotationCollector = new AnnotationCollector
    parser.setRDFHandler(annotationCollector)
    parser.parse(new FileInputStream(file), "http://pelagios.github.io/")
    annotationCollector.getAnnotations
  }
  
  def parsePleiadesRDF(files: Seq[File]): Seq[Place] = files.map(file => {
    val parser = Scalagios.getParser(file.getName)
    val placeCollector = new PlaceCollector      
    parser.setRDFHandler(placeCollector)
    parser.parse(new FileInputStream(file), "http://pelagios.github.io/")
    placeCollector.getPlaces     
  }).flatten
  
  def parsePleiadesRDF(is: InputStream, base: String, format: RDFFormat): Iterable[Place] = {
    val parser = RDFParserRegistry.getInstance.get(format).getParser
    val placeCollector = new PlaceCollector()      
    parser.setRDFHandler(placeCollector)
    parser.parse(is, base)
    placeCollector.getPlaces    
  }
  
}