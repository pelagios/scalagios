package org.pelagios.legacy

import java.io.{ File, FileInputStream }
import org.pelagios.Scalagios
import org.pelagios.legacy.api.{ GeoAnnotation, Place }
import org.pelagios.legacy.rdf.parser.{ AnnotationCollector, PlaceCollector}
import java.io.InputStream
import org.openrdf.rio.RDFFormat
import org.openrdf.rio.RDFParserRegistry
import org.openrdf.rio.RDFParser
import org.openrdf.rio.Rio
import org.openrdf.rio.UnsupportedRDFormatException

/** Legacy import utilities. **/
object LegacyInterop {
  
  /** Parses a data dump in the old OAC-based Pelagios format into the legacy API.
    *
    * @param file the legacy dump file
    * @return a list of legacy [[org.pelagios.legacy.api.GeoAnnotation]] objects  
    */
  def parseOAC(file: File): Iterable[GeoAnnotation] = {
    val format = Scalagios.guessFormatFromFilename(file.getName)
    if (format.isDefined) {
      val parser = Rio.createParser(format.get)
      val annotationCollector = new AnnotationCollector
      parser.setRDFHandler(annotationCollector)
      parser.parse(new FileInputStream(file), "http://pelagios.github.io/")
      annotationCollector.getAnnotations
    } else {
      throw new UnsupportedRDFormatException("Cannot determine RDF format for " + file.getName)      
    }
  }
  
  def parsePleiadesRDF(files: Seq[File]): Seq[Place] = files.map(file => {
    val format = Scalagios.guessFormatFromFilename(file.getName)
    if (format.isDefined) {
      val parser = Rio.createParser(format.get)
      val placeCollector = new PlaceCollector      
      parser.setRDFHandler(placeCollector)
      parser.parse(new FileInputStream(file), "http://pelagios.github.io/")
      placeCollector.getPlaces
    } else {
      throw new UnsupportedRDFormatException("Cannot determine RDF format for " + file.getName)  
    }
  }).flatten
  
  def parsePleiadesRDF(is: InputStream, base: String, format: RDFFormat): Iterable[Place] = {
    val parser = RDFParserRegistry.getInstance.get(format).getParser
    val placeCollector = new PlaceCollector()      
    parser.setRDFHandler(placeCollector)
    parser.parse(is, base)
    placeCollector.getPlaces    
  }
  
}