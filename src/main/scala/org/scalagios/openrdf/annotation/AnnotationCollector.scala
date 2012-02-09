package org.scalagios.openrdf.annotation

import scala.collection.mutable.HashMap

import org.openrdf.model.Statement
import org.openrdf.model.vocabulary.RDF
import org.openrdf.rio.helpers.RDFHandlerBase

import org.scalagios.openrdf.ParseStats
import org.scalagios.model.impl.DefaultGeoAnnotation
import org.scalagios.vocab.OAC

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