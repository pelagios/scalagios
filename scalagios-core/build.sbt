name := "scalagios-core"

version := "2.0.0"

scalaVersion := "2.10.0"

resolvers += "Open Source Geospatial Foundation Repository" at "http://download.osgeo.org/webdav/geotools/"

/** Runtime dependencies **/
libraryDependencies ++= Seq(
  "org.openrdf.sesame" % "sesame-rio-n3" % "2.7.5",
  "org.openrdf.sesame" % "sesame-rio-rdfxml" % "2.7.5",   
  "org.fusesource.scalate" % "scalate-core_2.10" % "1.6.1",           
  "com.vividsolutions" % "jts" % "1.13",
  "org.geotools" % "gt-geojson" % "10.5",
  "net.liftweb" %% "lift-json" % "2.5",
  "org.slf4j" % "slf4j-simple" % "1.7.7",
  "com.typesafe.slick" %% "slick" % "2.0.2",
  "com.h2database" % "h2" % "1.4.178",
  "org.apache.lucene" % "lucene-analyzers-common" % "4.8.1",
  "org.apache.lucene" % "lucene-queryparser" % "4.8.1"
)

/** Test dependencies **/
libraryDependencies ++= Seq(
  "junit" % "junit" % "4.11" % "test",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
)
