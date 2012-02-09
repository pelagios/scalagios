package org.scalagios.api

/**
 * Pelagios <em>Dataset</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait Dataset {
  
  def uri: String

}

/**
 * A default (POJO-style) implementation of the Pelagios
 * <em>Dataset</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class DefaultDataset(var uri: String) extends Dataset {

}