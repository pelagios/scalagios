package org.pelagios.legacy.graph.exception

/**
 * An exception that indicates a serious problem with the Graph's integrity.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class GraphIntegrityException(msg: String) extends Exception(msg)
