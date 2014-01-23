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

    val sentences = document.get(classOf[CoreAnnotations.SentencesAnnotation]).asScala
    sentences.toSeq.map(sentence => {
      sentence.get(classOf[CoreAnnotations.TokensAnnotation]).asScala.toSeq.map(token => {
        val ne = token.get(classOf[CoreAnnotations.TextAnnotation])
        val category = token.get(classOf[CoreAnnotations.NamedEntityTagAnnotation])
        val offset = token.beginPosition  
        
        NamedEntity(ne, category, offset)
      })
    }).flatten    
  }
  
}

case class NamedEntity(term: String, category: String, offset: Int)