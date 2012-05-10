package org.scalagios.rdf.parser.validation

import scala.collection.mutable.ListBuffer
import org.openrdf.model.Statement
import org.scalagios.rdf.vocab.DCTerms
import org.openrdf.model.vocabulary.RDFS

private[parser] trait HasValidation {

  protected val issues = ListBuffer[ValidationIssue]()
  
  private val WRONG_LABEL_TYPE_RDFS_LABEL = "Please use dcterms:title to assign labels - not rdfs:label"
  private val DESCRIPTION_TOO_LONG = "Dataset descriptions should be 80 characters maximum"
  
  def validateAnnotations(statement: Statement): Unit = {
    val (subj, pred, obj) = (statement.getSubject(), statement.getPredicate(), statement.getObject())
    
    pred match {
      case RDFS.LABEL => issues.append(ValidationIssue(Severity.WARNING, WRONG_LABEL_TYPE_RDFS_LABEL))
      case _ => None 
    }
  }
  
  def validateDatasets(statement: Statement): Unit = {
    val (subj, pred, obj) = (statement.getSubject(), statement.getPredicate(), statement.getObject())
    
    pred match {
      case DCTerms.description => if (obj.stringValue().length > 80) 
        issues.append(ValidationIssue(Severity.WARNING, DESCRIPTION_TOO_LONG))
      case _ => None 
    }    
  }   
  
}