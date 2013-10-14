# Scalagios

Scalagios is a utility software library for working with data from the [PELAGIOS project](http://pelagios-project.blogspot.com) on
the Java Virtual Machine. Scalagios provides:

* a convenient programming API based on Pelagios' domain model primitives: _Datasets_, _AnnotatedThings_, _Annotations_, _Places_, etc.
* utilities to parse Pelagios data and gazetteer dump files into a domain model graph structure
* utilities to work with Pelagios "legacy data" (from Pelagios project phases 1 & 2)
* graph database I/O utilities based on [Tinkerpop Blueprints](http://tinkerpop.com/)

## License

Scalagios is licensed under the [GNU General Public License v3.0](http://www.gnu.org/licenses/gpl.html).

## Getting Started

Reading Pelagios RDF data from a file:

```scala
val data: Iterable[AnnotatedThing] = Scalagios.readFromFile(new File("data-file.ttl"))

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

Scalagios.writeToFile(annotatedThing, new File("data-file.ttl")
```

## Developer Information

Scalagios is written in [Scala](http://www.scala-lang.org) and built with [SBT](http://www.scala-sbt.org/).

* To build the library, run `sbt package`.
* To run the unit tests, use `sbt test`
* To generate an Eclipse project, run `sbt eclipse`.
* To generate ScalaDoc, run `sbt doc`.  (Docs will be in `target/scala-2.10/api/`)

__Note:__ dependency download may take a while the first time you build the project!
