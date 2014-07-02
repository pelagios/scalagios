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

  /** Builds the dataset hierarchy and returns the top-level datasets **/
  lazy val datasets: Iterable[Dataset] = {
    val allDatasets =
      resourcesOfType(VoID.Dataset, Seq(_.hasPredicate(VoID.dataDump)))
        .map(new DatasetResource(_))
  
    val subsetLookupTable: Map[Dataset, Seq[String]] =
      allDatasets
        .filter(_.resource.hasPredicate(VoID.subset))
        .map(parent => (parent, parent.resource.get(VoID.subset).map(_.stringValue))).toMap
      
    val parentLookupTable: Map[String, Dataset] =
      subsetLookupTable.flatMap(tuple => tuple._2.map(uri => (uri, tuple._1)))      
    
    // Step 1: wire parent/child relations
    allDatasets.foreach(dataset => {
      dataset.subsets = subsetLookupTable.get(dataset).getOrElse(Seq.empty[String])
        .map(uri => allDatasets.find(_.uri == uri)).filter(_.isDefined).map(_.get)
       
      dataset.isSubsetOf = parentLookupTable.get(dataset.uri)  
    })
    
    allDatasets.filter(_.isSubsetOf.isEmpty)
  }
      
}

private[parser] class DatasetResource(val resource: Resource) extends Dataset {

  def uri: String = resource.uri
  
  def title: String = resource.getFirst(DCTerms.title).map(_.stringValue).getOrElse("[NO TITLE]") // 'NO TITLE' should never happen!

  def publisher: String = resource.getFirst(DCTerms.publisher).map(_.stringValue).getOrElse("[NO PUBLISHER]") // 'NO PUBLISHER' should never happen!
  
  def license: String = resource.getFirst(DCTerms.license).map(_.stringValue).getOrElse("[NO LICENSE]") // 'NO LICENSE' should never happen!
  
  var isSubsetOf: Option[Dataset] = None
    
  def description: Option[String] = resource.getFirst(DCTerms.description).map(_.stringValue)
 
  def homepage: Option[String] = resource.getFirst(FOAF.homepage).map(_.stringValue)
  
  def subjects: Seq[String] = resource.get(DCTerms.subject).map(_.stringValue)
  
  def datadumps: Seq[String] = resource.get(VoID.dataDump).map(_.stringValue)
  
  var subsets: Seq[Dataset] = Seq.empty[Dataset]
  
}
