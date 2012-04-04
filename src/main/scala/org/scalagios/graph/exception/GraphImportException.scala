package org.scalagios.graph.io

/**
 * An exception that indicates a serious problem during data import.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class GraphImportException(msg: String) extends Exception(msg) 