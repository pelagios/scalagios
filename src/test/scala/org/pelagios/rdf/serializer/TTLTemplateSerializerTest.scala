package org.pelagios.rdf.serializer

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfter
import org.pelagios.api._

@RunWith(classOf[JUnitRunner])
class TTLTemplateSerializerTest extends FunSuite {
  
  test("Test TTL template rendering") {
    val thing = new DefaultAnnotatedThing("http://example.org/pelagios/annotated-things/1", "My EGD")
    val annotations = Seq.range(0, 10).map(idx => {
      val a = new DefaultAnnotation("http://example.org/pelagios/annotations/" + idx, thing)
      a.place = Seq("http://pleiades.stoa.org/places/1234567")
      a.transcription = Some(new Transcription("Athens", Transcription.Toponym))
      thing.annotations = thing.annotations :+ a
      a
    })
    
    val ttl = TTLTemplateSerializer.toString(Seq(thing))
    println(ttl)
  }
  
}