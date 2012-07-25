package org.scalagios.rdf.parser

import scala.collection.mutable.HashMap
import org.openrdf.model.Statement
import org.openrdf.model.vocabulary.RDF
import org.openrdf.rio.helpers.RDFHandlerBase
import org.scalagios.rdf.parser.validation.HasValidation
import org.scalagios.api.{DefaultGeoAnnotation, DefaultGeoAnnotationTarget}
import org.scalagios.rdf.vocab.{DC, DCTerms, FOAF, OAC, VoID} 
import org.scalagios.api.DefaultGeoAnnotationTarget
import org.openrdf.model.vocabulary.RDFS
import org.scalagios.rdf.parser.validation.{ValidationIssue, Severity}

/**
 * A default OACEntity implementation we can use as placeholder as long is we don't 
 * know what type of entity we are dealing with
 */
class DefaultOACEntity(var uri:String) { var title: Option[String] = None } 

/**
 * Analogous to the OpenRDF <em>StatementCollector</em>, this RDFHandler
 * implementation collects GeoAnnotations and GeoAnnotationTargets from a
 * Pelagios RDF data file into a List.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class AnnotationCollector extends RDFHandlerBase with HasStatistics with HasValidation {
  
  // A utility type that covers common properties of GeoAnnotations and GeoAnnotationTargets 
  type OACEntity = { var uri: String; var title: Option[String] }
  
  // Utility method to convert from OACEntity place holder to concrete object
  private def _convert[T<: OACEntity](entity: OACEntity, clazz: Class[T]): T = {
    val converted = clazz.getConstructors()(0).newInstance(entity.uri).asInstanceOf[T]
    converted.title = entity.title
    converted
  }    

  private val annotationBuffer = new HashMap[String, OACEntity]
  
  def annotationsTotal = annotationBuffer.filter(_._2.isInstanceOf[DefaultGeoAnnotation]).size

  def getAnnotations = annotationBuffer.values.filter(_.isInstanceOf[DefaultGeoAnnotation])
    .map(_.asInstanceOf[DefaultGeoAnnotation])

  override def handleStatement(statement: Statement): Unit = {
    triplesTotal += 1
    validateAnnotations(statement)
    
    val (subj, obj) = (statement.getSubject().stringValue(), statement.getObject())
    
    (statement.getPredicate(), obj) match {
      case (RDF.TYPE, OAC.Annotation)  => getOrCreate(subj, classOf[DefaultGeoAnnotation])
      case (RDF.TYPE, OAC.Target) => getOrCreate(subj, classOf[DefaultGeoAnnotationTarget])
      case (OAC.hasBody, _) =>
          getOrCreate(subj, classOf[DefaultGeoAnnotation]).asInstanceOf[DefaultGeoAnnotation].body = obj.stringValue
      case (OAC.hasTarget, _) => 
        getOrCreate(subj, classOf[DefaultGeoAnnotation]).asInstanceOf[DefaultGeoAnnotation].target = 
          getOrCreate(obj.stringValue, classOf[DefaultGeoAnnotationTarget]).asInstanceOf[DefaultGeoAnnotationTarget]       
      case (FOAF.thumbnail, _) => 
        getOrCreate(subj, classOf[DefaultGeoAnnotationTarget]).asInstanceOf[DefaultGeoAnnotationTarget].thumbnail = 
          Some(obj.stringValue)
      // NOTE: we support rdfs:label for titles, but it's deprecated - use dcterms:title instead!
      case (RDFS.LABEL, _) => getOrCreate(subj, classOf[DefaultOACEntity]).title = 
        Some(obj.stringValue)
      // NOTE: we support dc:title for titles, but it's deprecated - use dcterms:title instead!      
      case (DC.title, _) => getOrCreate(subj, classOf[DefaultOACEntity]).title = 
        Some(obj.stringValue)
      case (DCTerms.title, _) => getOrCreate(subj, classOf[DefaultOACEntity]).title = 
        Some(obj.stringValue)
      case (VoID.inDataset, _) => getOrCreate(subj, classOf[DefaultGeoAnnotation]).asInstanceOf[DefaultGeoAnnotation].inDataset =
        Some(obj.stringValue)
      
      case _ => triplesSkipped += 1
    }
  }
   
  private def getOrCreate[T<: OACEntity](uri: String, clazz: Class[T]): OACEntity = {
    annotationBuffer.get(uri) match {
      case Some(a) => {
        if (a.isInstanceOf[DefaultOACEntity] && !clazz.isInstanceOf[DefaultOACEntity]) {
          // Convert the placeholder to the specified type
          val concrete = _convert(a, clazz)
          annotationBuffer.put(uri, concrete)
          concrete
        } else {
          // Object is not a placeholder any more - return it as the specified type
          a
        }
      }
      case None =>  {
        val a = clazz.getConstructors()(0).newInstance(uri).asInstanceOf[T]
        annotationBuffer.put(uri, a)
        a
      }
    }
  }
  
}