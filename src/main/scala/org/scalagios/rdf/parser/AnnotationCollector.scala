package org.scalagios.rdf.parser

import scala.collection.mutable.HashMap

import org.openrdf.model.Statement
import org.openrdf.model.vocabulary.RDF
import org.openrdf.rio.helpers.RDFHandlerBase

import org.scalagios.rdf.vocab.OAC
import org.scalagios.rdf.parser.validation.HasValidation
import org.scalagios.api.{DefaultGeoAnnotation, DefaultGeoAnnotationTarget}

/**
 * Analogous to the OpenRDF <em>StatementCollector</em>, this RDFHandler
 * implementation collects GeoAnnotations from a Pelagios RDF data file
 * into a List.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class AnnotationCollector extends RDFHandlerBase with HasStatistics with HasValidation {
  
  private val annotationBuffer = new HashMap[String, DefaultGeoAnnotation]
  
  def annotationsTotal = annotationBuffer.size
  def getAnnotations = annotationBuffer.values

  override def handleStatement(statement: Statement): Unit = {
    triplesTotal += 1
    validateAnnotations(statement)
    
    val (subj, obj) = (statement.getSubject().stringValue(), statement.getObject())
    
    (statement.getPredicate(), obj) match {
      case (RDF.TYPE, OAC.Annotation)  => getOrCreate(subj)
      case (OAC.hasBody, _) => getOrCreate(subj).body = obj.stringValue()  
      case (OAC.hasTarget, _) => getOrCreate(subj).target = new DefaultGeoAnnotationTarget(obj.stringValue())    
      case _ => triplesSkipped += 1
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