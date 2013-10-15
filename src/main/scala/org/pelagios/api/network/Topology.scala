package org.pelagios.api.network

/** Represents the topological arrangements of annotations within a document.
  *
  * Topology could be one-dimensional, e.g. representing the linear sequence of
  * annotations within a book; or two dimensional, e.g. representing the network
  * of places on a graphical itinerary.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at> 
  */
case class Topology(val links: Seq[Edge])