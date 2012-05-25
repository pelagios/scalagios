package org.scalagios.rdf.parser

import scala.collection.mutable.HashMap

import org.openrdf.model.Statement
import org.openrdf.model.vocabulary.RDF
import org.openrdf.rio.helpers.RDFHandlerBase

import org.scalagios.api.{Dataset, DefaultDataset}
import org.scalagios.rdf.vocab.{VoID, DCTerms, FOAF, Formats}
import org.scalagios.rdf.parser.validation.HasValidation

/**
 * Analogous to the OpenRDF <em>StatementCollector</em>, this RDFHandler
 * implementation builds a Pelagios Dataset hierarchy from a VoID RDF file.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class DatasetCollector extends RDFHandlerBase with HasStatistics with HasValidation {
  
  /**
   * We'll use the instantiation time of the collector as timestamp for the 'lastUpdate'
   * property of all Datasets we parse
   */
  private val startTime = System.currentTimeMillis
  
  /**
   * Maps a Dataset URI to the Dataset
   */
  private val datasetBuffer = new HashMap[String, DefaultDataset]
  
  /**
   * Maps a the URI of a Dataset to its parent Dataset
   */
  private val parentsBuffer = new HashMap[String, DefaultDataset]
 
  private var rootDatasets: List[Dataset] = _
  
  def datasetsTotal = datasetBuffer.size
  
  def getRootDatasets: List[Dataset] = rootDatasets 

  override def handleStatement(statement: Statement): Unit = {
    triplesTotal += 1
    validateDatasets(statement)
    
    val (subj, pred, obj) = (statement.getSubject().stringValue(), statement.getPredicate(), statement.getObject())
    
    (pred, obj) match {
      case (RDF.TYPE, VoID.Dataset) => getOrCreate(subj)
      case (DCTerms.title, _) => getOrCreate(subj).title = obj.stringValue
      case (DCTerms.description, _) => getOrCreate(subj).description = Some(obj.stringValue)
      case (DCTerms.license, _) => getOrCreate(subj).license = Some(obj.stringValue)
      case (FOAF.homepage, _) => getOrCreate(subj).homepage = Some(obj.stringValue)
      case (VoID.dataDump, _) => getOrCreate(subj).associatedDatadumps ::= obj.stringValue
      case (VoID.uriSpace, _) => getOrCreate(subj).associatedUriSpace = Some(obj.stringValue)
      case (VoID.uriRegexPattern, _) => getOrCreate(subj).associatedRegexPattern = Some(obj.stringValue)
      case (VoID.subset, _) => parentsBuffer.put(obj.stringValue, getOrCreate(subj))
      case _ => triplesSkipped += 1
    }
  }
  
  override def endRDF(): Unit = {
    rootDatasets = List[Dataset]()
    
    datasetBuffer.values.foreach(dataset => {
      if (!dataset.isValid)
        throw new RuntimeException("Invalid dataset: " + dataset.uri)
      
      val parent = parentsBuffer.remove(dataset.uri)
      if (!parent.isEmpty) {
        parent.get.subsets = dataset :: parent.get.subsets
        dataset.parent = parent
      } else {
        rootDatasets = dataset :: rootDatasets
      }
    })    
  }
    
  private def getOrCreate(uri: String): DefaultDataset = {
    datasetBuffer.get(uri) match {
      case Some(d) => d
      case None =>  {
        val d = new DefaultDataset(uri)
        d.lastUpdated = startTime
        datasetBuffer.put(uri, d)
        d
      }
    }
  }
  
}