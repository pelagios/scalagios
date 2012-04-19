package org.scalagios.rdf.parser

import scala.collection.mutable.HashMap
import org.openrdf.model.Statement
import org.openrdf.model.vocabulary.RDF
import org.openrdf.rio.helpers.RDFHandlerBase
import org.scalagios.rdf.vocab.OAC
import org.scalagios.rdf.parser.validation.HasValidation
import org.scalagios.api.{DefaultGeoAnnotation, DefaultGeoAnnotationTarget}
import org.scalagios.rdf.vocab.DCTerms
import org.scalagios.rdf.vocab.FOAF
import org.scalagios.api.DefaultGeoAnnotationTarget

/**
 * Analogous to the OpenRDF <em>StatementCollector</em>, this RDFHandler
 * implementation collects GeoAnnotations from a Pelagios RDF data file
 * into a List.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class AnnotationCollector extends RDFHandlerBase with HasStatistics with HasValidation {
  
  // A common type that covers common properties of OAC Annotations and Targets 
  type OACEntity = { var uri: String; var title: Option[String] }
  
  // A default implementation we can use as placeholder for both
  class DefaultOACEntity(var uri:String) { var title: Option[String] = None } 
  
  // A companion with a utility method to convert from place holder to concrete object
  object DefaultOACEntity {
    def convert[T<: OACEntity](entity: OACEntity, clazz: Class[T]): T = {
      if (clazz.isInstanceOf[DefaultGeoAnnotation]) {
        val annotation = new DefaultGeoAnnotation(entity.uri)
        annotation.title = entity.title
        annotation.asInstanceOf[T]
      } else if (clazz.isInstanceOf[DefaultGeoAnnotationTarget]) {
        val target = new DefaultGeoAnnotationTarget(entity.uri)
        target.title = entity.title
        target.asInstanceOf[T]
      } else {
        throw new RuntimeException()
      }
    }    
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
      // Create a GeoAnnotation in the list (if it doesn't exist already)
      case (RDF.TYPE, OAC.Annotation)  => getOrCreate(subj, classOf[DefaultGeoAnnotation])
      
      // Create a GeoAnnotationTarget in the list (if it doesn't exist already)
      case (RDF.TYPE, OAC.Target) => getOrCreate(subj, classOf[DefaultGeoAnnotationTarget])
      
      // HasBody only allowed for GeoAnnotations -- get or create and add as String property 
      case (OAC.hasBody, _) => 
        getOrCreate(subj, classOf[DefaultGeoAnnotation]).asInstanceOf[DefaultGeoAnnotation].body = 
          obj.stringValue()
      
      // HasTarget only allowed for GeoAnnotations -- get or create annotation, and then get or create the target
      case (OAC.hasTarget, _) => 
        getOrCreate(subj, classOf[DefaultGeoAnnotation]).asInstanceOf[DefaultGeoAnnotation].target = 
          getOrCreate(obj.stringValue, classOf[DefaultGeoAnnotationTarget]).asInstanceOf[DefaultGeoAnnotationTarget]       
 
      // Thumbnail only allowed for GeoAnnotationTargets
      case (FOAF.thumbnail, _) => 
        getOrCreate(subj, classOf[DefaultGeoAnnotationTarget]).asInstanceOf[DefaultGeoAnnotationTarget].thumbnail = 
          Some(obj.stringValue)
      
      // This could be an annotation OR a target
      case (DCTerms.title, _) => getOrCreate(subj, classOf[DefaultOACEntity]).title = 
        Some(obj.stringValue())
      
      case _ => triplesSkipped += 1
    }
  }
   
  private def getOrCreate[T<: OACEntity](uri: String, clazz: Class[T]): OACEntity = {
    annotationBuffer.get(uri) match {
      case Some(a) => {
        if (a.isInstanceOf[DefaultOACEntity] && !clazz.isInstanceOf[DefaultOACEntity]) {
          // Convert the placeholder to the specified type
          val concrete = DefaultOACEntity.convert(a, clazz)
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