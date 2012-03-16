package org.scalagios.graph

import org.scalagios.api.Dataset
import com.tinkerpop.frames.Property
import com.tinkerpop.frames.Relation
import com.tinkerpop.frames.Direction

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
  
  @Relation(label="subset", direction=Direction.STANDARD)
  def subsets: List[DatasetVertex]

}