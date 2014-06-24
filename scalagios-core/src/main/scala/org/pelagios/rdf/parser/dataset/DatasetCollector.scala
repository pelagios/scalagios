package org.pelagios.rdf.parser.dataset

import org.pelagios.api.dataset.Dataset
import org.pelagios.rdf.vocab.{ DCTerms, VoID, FOAF }
import org.pelagios.rdf.parser.Resource
import org.pelagios.rdf.parser.Resource
import org.pelagios.rdf.parser.ResourceCollector

/** An implementation of [[org.pelagios.rdf.parser.ResourceCollector]] to handle VoID dataset descriptions.
  * 
  * TODO build dataset hierarchy
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
class DatasetCollector extends ResourceCollector {
  
  lazy val datasets: Iterator[Dataset] =
    resourcesOfType(VoID.Dataset, Seq(_.hasPredicate(VoID.dataDump)))
      .map(new DatasetResource(_))

}

private[parser] class DatasetResource(val resource: Resource) extends Dataset {

  def uri: String = resource.uri
  
  def title: String = resource.getFirst(DCTerms.title).map(_.stringValue).getOrElse("[NO TITLE]") // 'NO TITLE' should never happen!

  def publisher: String = resource.getFirst(DCTerms.publisher).map(_.stringValue).getOrElse("[NO PUBLISHER]") // 'NO PUBLISHER' should never happen!
  
  def license: String = resource.getFirst(DCTerms.license).map(_.stringValue).getOrElse("[NO LICENSE]") // 'NO LICENSE' should never happen!
    
  def description: Option[String] = resource.getFirst(DCTerms.description).map(_.stringValue)
 
  def homepage: Option[String] = resource.getFirst(FOAF.homepage).map(_.stringValue)
  
  def subjects: Seq[String] = resource.get(DCTerms.subject).map(_.stringValue)
  
  def datadumps: Seq[String] = resource.get(VoID.dataDump).map(_.stringValue)
  
}