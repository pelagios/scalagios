package org.pelagios.legacy

import java.io.{ File, FileInputStream }
import org.pelagios.Scalagios
import org.pelagios.legacy.api.{ GeoAnnotation, Place }
import org.pelagios.legacy.rdf.parser.{ AnnotationCollector, PlaceCollector}

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
  
  def parsePleiadesDumps(files: Seq[File]): Seq[Place] = files.map(file => {
    println("Parsing file " + file.getName)
    
    val parser = Scalagios.getParser(file.getName)
    val placeCollector = new PlaceCollector      
    parser.setRDFHandler(placeCollector)
    parser.parse(new FileInputStream(file), "http://pelagios.github.io/")
    placeCollector.getPlaces     
  }).flatten
  
}