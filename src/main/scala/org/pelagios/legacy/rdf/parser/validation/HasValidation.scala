package org.pelagios.legacy.rdf.parser.validation

import scala.collection.mutable.ListBuffer
import org.openrdf.model.Statement
import org.pelagios.legacy.rdf.vocab.DCTerms
import org.openrdf.model.vocabulary.RDFS
import org.pelagios.legacy.rdf.vocab.VoID
import org.pelagios.legacy.rdf.vocab.DC

private[parser] trait HasValidation {

  val issues = ListBuffer[ValidationIssue]()
  
  private val DESCRIPTION_TOO_LONG = "Dataset descriptions should be 80 characters maximum"
  private val IN_DATASET = "Please use void:dataDump and/or void:uriSpace/uriRegExPattern - void:inDataset. " + 
    "void:inDataset is meant to assciate entire dumpfiles with a void:Dataset, not individual annotations!"
  private val WRONG_LABEL_TYPE_RDFS_LABEL = "Please use dcterms:title to assign labels - not rdfs:label"
  private val WRONG_LABEL_TYPE_DC_TITLE = "Please use dcterms:title to assign labels - not dc:title"

  def validateAnnotations(statement: Statement): Unit = {
    val (subj, pred, obj) = (statement.getSubject(), statement.getPredicate(), statement.getObject())
    
    pred match {
      case RDFS.LABEL => issues.append(ValidationIssue(Severity.WARNING, WRONG_LABEL_TYPE_RDFS_LABEL))
      case DC.title => issues.append(ValidationIssue(Severity.WARNING, WRONG_LABEL_TYPE_DC_TITLE))
      case VoID.inDataset => issues.append(ValidationIssue(Severity.WARNING, IN_DATASET))
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