name := "scalagios-core"

version := "2.0.7"

scalaVersion := "2.11.7"

resolvers += "Open Source Geospatial Foundation Repository" at "http://download.osgeo.org/webdav/geotools/"

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"

/** Runtime dependencies **/
libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-parser-combinators" % "2.11.0-M4",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5",
  "org.openrdf.sesame" % "sesame-rio-n3" % "2.8.5",
  "org.openrdf.sesame" % "sesame-rio-rdfxml" % "2.8.5",
  "org.openrdf.sesame" % "sesame-rio-turtle" % "2.8.5",
  "org.openrdf.sesame" % "sesame-rio-jsonld" % "2.8.5",
  "com.github.jsonld-java" % "jsonld-java" % "0.12.0",
  "org.scalatra.scalate" % "scalate-core_2.11" % "1.7.0",
  "com.vividsolutions" % "jts" % "1.13",
  "org.geotools" % "gt-geojson" % "10.5",
  "org.slf4j" % "slf4j-simple" % "1.7.7",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "com.h2database" % "h2" % "1.4.178",
  "org.apache.lucene" % "lucene-analyzers-common" % "4.8.1",
  "org.apache.lucene" % "lucene-queryparser" % "4.8.1"
)

/** Test dependencies **/
libraryDependencies ++= Seq(
  "junit" % "junit" % "4.11" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.1.3" % "test"
)
