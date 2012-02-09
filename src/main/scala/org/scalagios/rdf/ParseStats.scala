package org.scalagios.rdf.pleiades

import org.openrdf.rio.helpers.RDFHandlerBase

private[rdf] trait ParseStats {
  
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