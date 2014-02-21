package org.pelagios.tools.geoparsing

import edu.stanford.nlp.pipeline.{ Annotation, StanfordCoreNLP }
import edu.stanford.nlp.ling.CoreAnnotations
import java.util.Properties
import scala.collection.JavaConverters._

object GeoParser {

  val props = new Properties
  props.put("annotators", "tokenize, ssplit, pos, lemma, ner") // , parse, dcoref")
  val pipeline = new StanfordCoreNLP(props)
  
  def parse(text: String): Seq[NamedEntity] = {
    val document = new Annotation(text)
    pipeline.annotate(document)

    val sentences = document.get(classOf[CoreAnnotations.SentencesAnnotation])
    sentences.asScala.toSeq.map(sentence => {
	  val tokens = sentence.get(classOf[CoreAnnotations.TokensAnnotation]).asScala.toSeq
	  tokens.foldLeft(Seq.empty[NamedEntity])((result, nextToken) => {
		val previousNE = if (result.size > 0) Some(result.head) else None		
        val term = nextToken.get(classOf[CoreAnnotations.TextAnnotation])
        val category = nextToken.get(classOf[CoreAnnotations.NamedEntityTagAnnotation])
        val offset = nextToken.beginPosition  

        if (previousNE.isDefined && previousNE.get.category == category) {
		  NamedEntity(previousNE.get.term + " " + term, category, previousNE.get.offset) +: result.tail
	    } else {
		  NamedEntity(term, category, offset) +: result
	    }        
      })
	}).flatten    
  }
  
}

case class NamedEntity(term: String, category: String, offset: Int)
