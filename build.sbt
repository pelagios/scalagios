name := "scalagios"

version := "2.0.0-alpha"

scalaVersion := "2.10.0"

libraryDependencies ++= Seq(
  /**
    * Logging
    */
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.clapper" % "grizzled-slf4j_2.10" % "1.0.1",
  /** 
    * Geo
    */
  "com.vividsolutions" % "jts" % "1.13",
  /** 
    * CLI argument parsing
    */
  "com.github.scopt" %% "scopt" % "3.1.0",
  /** 
    * RDF
    */
  "org.openrdf.sesame" % "sesame-rio-n3" % "2.7.5",
  "org.openrdf.sesame" % "sesame-rio-rdfxml" % "2.7.5",
  /** 
    * Indexing
    */  
  "org.apache.lucene" % "lucene-analyzers-common" % "4.4.0",
  "org.apache.lucene" % "lucene-queryparser" % "4.4.0",
  /** 
    * Graph API
    */  
  "com.tinkerpop.blueprints" % "blueprints-neo4j-graph" % "1.2",
  "com.tinkerpop.blueprints" % "blueprints-neo4jbatch-graph" % "1.2",
  /**
    * Test
    */
  "junit" % "junit" % "4.11" % "test",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
)
