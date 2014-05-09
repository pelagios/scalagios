package org.pelagios.rdf.serializer

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfter
import org.pelagios.api.annotation._

@RunWith(classOf[JUnitRunner])
class TTLTemplateSerializerTest extends FunSuite {
  
  test("Test TTL template rendering") {
    val thing: AnnotatedThing = AnnotatedThing("http://example.org/pelagios/annotated-things/1", "My EGD", sources = "foo")
    val annotations = Seq.range(0, 10).foreach(idx => {
      val a = Annotation("http://example.org/pelagios/annotations/" + idx, thing,
                         place = "http://pleiades.stoa.org/places/1234567",
                         transcription = Transcription("Athens", TranscriptionType.Toponym))
    })
    
    val ttl = TTLTemplateSerializer.toString(Seq(thing))
    println(ttl)
  }
  
}