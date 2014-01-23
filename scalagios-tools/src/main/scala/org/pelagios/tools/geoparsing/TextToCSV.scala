package org.pelagios.tools.geoparsing

import java.io.{ File, FileInputStream, PrintWriter }
import java.util.zip.GZIPInputStream
import org.openrdf.rio.RDFFormat
import org.pelagios.Scalagios
import org.pelagios.gazetteer.PlaceIndex
import org.pelagios.tools.georesolution.GeoResolver
import scala.io.Source

/**
 * Converts a plaintext file to CSV.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
object TextToCSV extends App {
  
  val inputFile = new File("/home/simonr/Arbeitsfläche/Ammianus/texts/")
    
  private val INDEX_DIR = "index"
  private val GAZETTEER_DATA_PATH = "test-data/pleiades-20120826-migrated.ttl.gz"

  println()
  println("#######################################")
  println("# Scalagios Text-to-CSV converter")
  println("#######################################")
    
  val idx = PlaceIndex.open(INDEX_DIR)
  if (idx.isEmpty) {
    println("There is no gazetter index yet - just a second...")     
    println("Loading Pleiades data")
    val pleiades = Scalagios.parseGazetteer(new GZIPInputStream(new FileInputStream(GAZETTEER_DATA_PATH)), "http://pleiades.stoa.org/", RDFFormat.TURTLE)
    println("Building index")
    idx.addPlaces(pleiades)      
    println("Index complete")          
  }
  
  val (files, outputFile) = if (inputFile.isDirectory())
      (inputFile.listFiles().toSeq, inputFile.getAbsolutePath + ".csv")
    else
      (Seq(inputFile), inputFile.getAbsolutePath.substring(0, inputFile.getAbsolutePath.lastIndexOf(".")) + ".csv")       
  
  val csv = files.par.map(f => {
    println("\n#######################################")
    println("### GeoParsing: " + f.getAbsolutePath + "\n")      
  
    val text = Source.fromFile(f, "UTF-8").getLines().mkString("\n")
    val namedEntities = GeoParser.parse(text)
    val toponyms = namedEntities.filter(_.category == "LOCATION") 
    
    println("\n#######################################")
    println("### Geo-Parsing complete")
    println("Identified " + toponyms.size + " toponyms")

    println("\n#######################################")
    println("### Geo-resultion")
    val resolver = new GeoResolver(idx)
    val gazetteerMatches = resolver.matchToponymList(toponyms.map(toponym => Some(toponym.term)))

    println("\n#######################################")
    println("### Writing results")
    val gdocPart = if (inputFile.isDirectory)
        Some(f.getName.substring(0, f.getName.lastIndexOf(".")))
      else
        None
        
    CSVSerializer.serialize(toponyms.zip(gazetteerMatches), gdocPart)
  })
  
  val printWriter = new PrintWriter(outputFile)
  printWriter.write(CSVSerializer.header)
  csv.seq.foreach(lines => printWriter.write(lines))
  printWriter.flush()
  printWriter.close()
  
  println("\n#######################################")  
  println("### Results written to: " + outputFile)

}