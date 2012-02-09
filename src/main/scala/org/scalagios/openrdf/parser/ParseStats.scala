package org.scalagios.openrdf.parser

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