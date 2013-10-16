package org.pelagios.api.layout

import org.pelagios.api.AnnotatedThing
import org.pelagios.api.DefaultAnnotatedThing

/** Represents the topological arrangements of annotations within a document.
  *
  * Topology could be one-dimensional, e.g. representing the linear sequence of
  * annotations within a book; or two dimensional, e.g. representing the network
  * of places on a graphical itinerary.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at> 
  */
case class Layout(val links: Seq[Link], val annotatedThing: AnnotatedThing) {
  
  // TODO ugly - figure out a better way to handle this!
  if (annotatedThing.isInstanceOf[DefaultAnnotatedThing])
    annotatedThing.asInstanceOf[DefaultAnnotatedThing].layout = Some(this)
  
}