name := "scalagios-legacy"

version := "2.0.0"

scalaVersion := "2.10.0"

/** Runtime dependencies **/
libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.clapper" % "grizzled-slf4j_2.10" % "1.0.1",
  "org.openrdf.sesame" % "sesame-rio-n3" % "2.7.5",
  "org.openrdf.sesame" % "sesame-rio-rdfxml" % "2.7.5",         
  "com.vividsolutions" % "jts" % "1.13",
  "com.tinkerpop.blueprints" % "blueprints-neo4j-graph" % "1.2",
  "com.tinkerpop.blueprints" % "blueprints-neo4jbatch-graph" % "1.2"
)

/** Test dependencies **/
libraryDependencies ++= Seq(
  "junit" % "junit" % "4.11" % "test",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
)
