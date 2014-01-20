# Scalagios

Scalagios is a collection of utilities for working with data from the [PELAGIOS project](http://pelagios-project.blogspot.com).
Primarily, Scalagios provides utility libraries for use with your own (Java Virtual Machine-based) software. But there are also a 
few command-line tools for elementary data processing tasks. Scalagios consists of four sub-projects:

* __scalagios-core__. Base functionality for handling Pelagios data in your own JVM applications.

* __scalagios-gazetteer__. A utility for setting up simple [Lucene](http://lucene.apache.org/core/)-based cross-gazetteer search infrastructure.

* __scalagios-tools__. Command-line tools based on Scalagios.

* __scalagios-legacy__. Helpers to process and convert legacy (OAC-based) annotations from Pelagios project phases 1 & 2.

Scalagios is written in the [Scala](http://www.scala-lang.org) programming language.

## scalagios-core

scalagios-core provides most of the base functionality needed to work with Pelagios data in your own JVM-based applications. It
includes a convenient programming API based on Pelagios' domain model primitives: _Annotations_, _AnnotatedThings_, _Places_,
_Locations_, etc., and provides utilities to read and write Pelagios annotation and gazetteer data to and from RDF.

Reading Pelagios RDF data from a file:

```scala
val data: Iterable[AnnotatedThing] = Scalagios.readFromFile("data-file.ttl")

data.foreach(thing => {
  thing.annotations.foreach(annotation => {
    // Do something with annotations
  })
})
```

Writing Pelagios RDF data to a file:

```scala
val thing = AnnotatedThing("http://pelagios.org/egds/01", "My EGD")

val annotation = Annotation("http://pelagios.org/egds/01/annotations/01", thing, 
                            place = "http://pleiades.stoa.org/places/423025",
                            transcription = "ROMA")

Scalagios.writeToFile(annotatedThing, "data-file.ttl")
```

## scalagios-gazetteer

[TODO...]

## scalagios-tools

[TODO...]

...to convert text to Recogito-compliant CSV run

``sbt tools/run`` 

## scalagios-legacy

* utilities to work with Pelagios "legacy data" 
* graph database I/O utilities based on [Tinkerpop Blueprints](http://tinkerpop.com/)


## Developer Information

Scalagios is written in [Scala](http://www.scala-lang.org) and built with [SBT](http://www.scala-sbt.org/).

* To build the sub-project libraries, run `sbt package`.
* To run the unit tests, use `sbt test`
* To generate an Eclipse project for each sub-project, run `sbt eclipse`.
* To generate ScalaDoc, run `sbt doc`.

__Note:__ dependency download may take a while the first time you build the project!

## License

Scalagios is licensed under the [GNU General Public License v3.0](http://www.gnu.org/licenses/gpl.html).
