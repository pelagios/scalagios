package org.pelagios.tools.geoparsing

import java.io.{ File, FileInputStream, PrintWriter }
import java.util.zip.GZIPInputStream
import org.openrdf.rio.RDFFormat
import org.pelagios.Scalagios
import org.pelagios.gazetteer.PlaceIndex
import org.pelagios.tools.georesolution.GeoResolver
import scala.io.Source
import java.awt.Frame
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.FileDialog

/**
 * Converts a plaintext file to CSV.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
object TextToCSV extends App {

  private val INDEX_DIR = "index"
  private val GAZETTEER_DATA_PATH = "test-data/pleiades-20120826-migrated.ttl.gz"
  
  val fileChooser = new FileChooser()
  val inputFile = fileChooser.getFile()
  fileChooser.dispose()
  
  if (inputFile.isEmpty) {
    println("Operation canceled by user.")
  } else {
    val input = inputFile.get
    println()
    println("#######################################") 
    println("# Scalagios Text-to-CSV converter")
    println("#######################################")
    
    val idx = PlaceIndex.open(INDEX_DIR) 
    if (idx.isEmpty)
      initIndex(idx)
  
    val (files, outputFile) = if (input.isDirectory())
        (input.listFiles().toSeq, input.getAbsolutePath + ".csv")
      else
        (Seq(input), input.getAbsolutePath.substring(0, input.getAbsolutePath.lastIndexOf(".")) + ".csv")       
  
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
      val gdocPart = if (input.isDirectory)
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
  
  private def initIndex(idx: PlaceIndex) = {
    println("There is no gazetter index yet - just a second...")     
    println("Loading Pleiades data")
    val pleiades = Scalagios.readPlaces(new GZIPInputStream(new FileInputStream(GAZETTEER_DATA_PATH)), "http://pleiades.stoa.org/", RDFFormat.TURTLE)
    println("Building index")
    idx.addPlaces(pleiades)      
    println("Index complete")          
  }

}

class FileChooser extends Frame {
  
  val fd = new FileDialog(this, "Open File or Directory", FileDialog.LOAD)
  fd.setVisible(true)
  
  def getFile(): Option[File] = {
    val f = fd.getFile()
    val dir = fd.getDirectory()
    if (f == null)
      None
    else
      Some(new File(dir, f))
  }
  
}