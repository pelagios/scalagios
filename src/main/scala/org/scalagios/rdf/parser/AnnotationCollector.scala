package org.scalagios.rdf.parser

import scala.collection.mutable.HashMap

import org.openrdf.model.Statement
import org.openrdf.model.vocabulary.RDF
import org.openrdf.rio.helpers.RDFHandlerBase

import org.scalagios.rdf.vocab.OAC
import org.scalagios.api.DefaultGeoAnnotation

/**
 * Analogous to the OpenRDF <em>StatementCollector</em>, this RDFHandler
 * implementation collects GeoAnnotations from a Pelagios RDF data file
 * into a List.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class AnnotationCollector extends RDFHandlerBase with ParseStats {
  
  private val annotationBuffer = new HashMap[String, DefaultGeoAnnotation]
  
  def annotationsTotal = annotationBuffer.size
  def getAnnotations = annotationBuffer.values

  override def handleStatement(statement: Statement): Unit = {
    triplesTotal += 1
    
    val subj = statement.getSubject().stringValue()
    val obj = statement.getObject()
    
    val annotation = (statement.getPredicate(), obj) match {
      case (RDF.TYPE, OAC.ANNOTATION)  => getOrCreate(subj)
      
      case (OAC.HAS_TARGET, _) => {
        val a = getOrCreate(subj)
        a.target = obj.stringValue()
        a
      }
      
      case (OAC.HAS_BODY, _) => {
        val a = getOrCreate(subj)
        a.body = obj.stringValue()
        a
      }
      
      case _ => {
        triplesSkipped += 1
        null
      }
    }
  }
   
  private def getOrCreate(uri: String): DefaultGeoAnnotation = {
    annotationBuffer.get(uri) match {
      case Some(a) => a
      case None =>  {
        val a = new DefaultGeoAnnotation(uri)
        annotationBuffer.put(uri, a)
        a
      }
    }
  }
  
}