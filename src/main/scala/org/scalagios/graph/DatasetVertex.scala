package org.scalagios.graph

import org.scalagios.api.Dataset
import com.tinkerpop.frames.Property

/**
 * An implementation of the Pelagios <em>Dataset</em> model primitive
 * backed by a Tinkerpop Graph Vertex.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait DatasetVertex extends Dataset {
  
  @Property("uri")
  def uri: String
  
  @Property("title")
  def title: String
  
  @Property("description")
  def description: String
  
  @Property("license")
  def license: String
  
  @Property("homepage")
  def homepage: String
  
  // TODO wire this up with relations to the subset vertices
  def subsets: List[DatasetVertex]

}