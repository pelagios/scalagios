package org.scalagios.rdf.parser

import scala.collection.mutable.HashMap
import org.openrdf.rio.helpers.RDFHandlerBase
import org.openrdf.model.Statement
import org.scalagios.api.{Dataset, DefaultDataset}
import org.openrdf.model.vocabulary.RDF
import org.scalagios.rdf.vocab.{VoID, DCTerms, FOAF}
import org.scalagios.rdf.vocab.Formats

/**
 * Analogous to the OpenRDF <em>StatementCollector</em>, this RDFHandler
 * implementation builds a Pelagios Dataset hierarchy from a VoID RDF file.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class DatasetCollector extends RDFHandlerBase with ParseStats {
  
  /**
   * Create a map that maps a Dataset's URI to a tuple containing (i) the 
   * Dataset itself, and (ii) a List of its subsets' URIs.
   */
  private val datasetBuffer = new HashMap[String, (DefaultDataset, List[String])]
  
  def datasetsTotal = datasetBuffer.size
  
  def debug = datasetBuffer.values
  
  /**
   * Returns the full Dataset hierarchy (starting with the top-level Dataset)
   * TODO return the top-level dataset
   */
  def getDataset: Option[Dataset] = None

  override def handleStatement(statement: Statement): Unit = {
    triplesTotal += 1
    
    val (subj, pred, obj) = (statement.getSubject().stringValue(), statement.getPredicate(), statement.getObject())
    
    (pred, obj) match {
      case (RDF.TYPE, VoID.Dataset) => getOrCreate(subj)
      case (DCTerms.title, _) => getOrCreate(subj)._1.title = obj.stringValue()
      case (DCTerms.description, _) => getOrCreate(subj)._1.description = obj.stringValue()
      case (DCTerms.license, _) => getOrCreate(subj)._1.license = obj.stringValue()
      case (FOAF.homepage, _) => getOrCreate(subj)._1.homepage = obj.stringValue()
      case (VoID.uriSpace, _) => getOrCreate(subj)._1.uriSpace = obj.stringValue()
      case (VoID.dataDump, _) => getOrCreate(subj)._1.datadump = obj.stringValue()
      case (VoID.feature, _) => {
        val d = getOrCreate(subj)._1
        val format = Formats.toRDFFormat(obj)
        if (format.isDefined)
        	d.dumpFormat = format.get        
      }
      case (VoID.subset, _) => {
        val (dataset, subsets) = getOrCreate(subj)
        datasetBuffer.put(subj, (dataset, obj.stringValue() :: subsets))
      }
      
      case _ => triplesSkipped += 1
    }

  }
  
  override def endRDF(): Unit = {
    
  }
    
  private def getOrCreate(uri: String): (DefaultDataset, List[String]) = {
    datasetBuffer.get(uri) match {
      case Some(d) => d
      case None =>  {
        val d = (new DefaultDataset(uri), List[String]())
        datasetBuffer.put(uri, d)
        d
      }
    }
  }
  
}