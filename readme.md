# Scalagios

Scalagios is a utility library for working with data from the [PELAGIOS project](http://pelagios-project.blogspot.com) on the Java Virtual Machine. Scalagios provides

* Domain model classes for Pelagios' core data primitives: _Places_, _Datasets_ and _GeoAnnotations_
* Custom RDF handlers for the [OpenRDF](http://www.openrdf.org/) Rio parser toolkit to read Pelagios
  data and Pleiades dump files with low memory overhead
* Graph database support:
  * Graph access and batch import utilities for [Neo4j](http://neo4j.org/) based on
   [Tinkerpop Blueprints](http://tinkerpop.com/)
  * Domain model bindings based on [Tinkerpop Frames](https://github.com/tinkerpop/frames/wiki)
* More to come...
  

## License

Scalagios is licensed under the [GNU General Public License v3.0](http://www.gnu.org/licenses/gpl.html).

## Developer Information

Scalagios is written in [Scala](http://www.scala-lang.org) and built with [Gradle](http://www.gradle.org).
(Download and installation instructions for Gradle are [here](http://www.gradle.org/installation.html)). 

* To build and test the project, run `gradle build`. (Test reports will be in `build/reports/tests`)
* To generate a jar package, run `gradle jar`. (The jar will be in `build/libs`)
* To generate an Eclipse project, run `gradle eclipse`.
* To generate ScalaDoc, run `gradle scaladoc`.  (Docs will be in `build/docs`)

__Note:__ dependency download may take a while the first time you build the project!