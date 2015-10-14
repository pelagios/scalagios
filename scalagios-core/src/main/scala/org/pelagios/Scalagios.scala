package org.pelagios

import org.openrdf.rio.{ RDFFormat, RDFParser, UnsupportedRDFormatException }
import org.openrdf.rio.n3.N3ParserFactory
import org.openrdf.rio.rdfxml.RDFXMLParserFactory
import org.openrdf.rio.turtle.TurtleParserFactory

/** A utility to parse & write Pelagios data.
  *
  * @author Rainer Simon <rainer.simon@ait.ac.at>  
  */
object Scalagios extends ScalagiosReader with ScalagiosWriter {
  
  private[pelagios] val TURTLE = "ttl"
  
  private[pelagios] val RDFXML = "rdf"
  
  private[pelagios] val N3 = "n3"
  
  /** OpenRDF requires a 'base URI' for parsing RDF from file - but doesn't actually seem to do anything with it **/
  private[pelagios] val BASE_URI = "http://pelagios.org"
  
  /** Returns the RDF Format for the specified extension **/
  def getFormatForExtension(extension: String): Option[RDFFormat] = extension.toLowerCase match {
    case e if e.endsWith(TURTLE) => Some(RDFFormat.TURTLE)
    case f if f.endsWith(RDFXML) => Some(RDFFormat.RDFXML)
    case f if f.endsWith(N3) => Some(RDFFormat.N3)
    case _ => None    
  }
  
  /** 'Guesses' the format from the specified filename. 
    *  
    * TODO in the future we want to make this more clever. But for the time
    * being, this function just distinguishes between uncompressed files and
    * files with a .gz extension, and the decides based on the extension
    * before the .gz suffix, if any.
    */
  def guessFormatFromFilename(filename: String): Option[RDFFormat] =
    if (filename.endsWith(".gz"))
      getFormatForExtension(filename.substring(0, filename.lastIndexOf('.')))
    else
      getFormatForExtension(filename)
  
}
