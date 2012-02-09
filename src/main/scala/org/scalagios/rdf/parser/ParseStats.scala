package org.scalagios.rdf.parser

/**
 * A helper trait that provides simple stats tracking
 * functionality for the RDFHandler implementations.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
private[parser] trait ParseStats {
  
  /**
   * Total number of triples counted in RDF document
   */
  var triplesTotal = 0
    
  /**
   * Total number of triples processed during import
   */
  protected var triplesSkipped = 0
  def triplesProcessed = triplesTotal - triplesSkipped

}