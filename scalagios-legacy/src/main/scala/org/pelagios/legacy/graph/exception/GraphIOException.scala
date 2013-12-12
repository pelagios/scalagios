package org.pelagios.legacy.graph.exception

/**
 * An exception that indicates a serious problem during data import.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class GraphIOException(msg: String) extends Exception(msg) 