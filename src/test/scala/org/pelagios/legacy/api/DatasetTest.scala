package org.pelagios.legacy.api

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DatasetTest extends FunSuite {

  /**
   * Construct a test dataset tree with the following hierarchy:
   * 
   * dataset0 --- dataset0_0 --- dataset0_0_0 --- <http://www.example.com/annotations/0001>
   *           |                               |- <http://www.example.com/annotations/0002>
   *           |
   *           |- dataset0_1 --- <http://www.example.com/annotations/0003>
   *           |
   *           |- dataset0_2 --- <http://www.example.com/annotations/0004>                                                    
   */
   
  val dataset0_0 = new DefaultDataset("http://www.example.com/datasets/0_0")
  val dataset0_0_0 = new DefaultDataset("http://www.example.com/datasets/0_0_0")
  dataset0_0_0.parent = Some(dataset0_0)
  dataset0_0_0.setAnnotations(List(
    new DefaultGeoAnnotation("http://www.example.com/annotations/0001"),
    new DefaultGeoAnnotation("http://www.example.com/annotations/0002")))
  dataset0_0.subsets = List(dataset0_0_0)

    
  val dataset0_1 = new DefaultDataset("http://www.example.com/datasets/0_1")
  dataset0_1.setAnnotations(List(new DefaultGeoAnnotation("http://www.example.com/annotations/0003")))

  
  val dataset0_2 = new DefaultDataset("http://www.example.com/datasets/0_2")
  dataset0_2.setAnnotations(List(new DefaultGeoAnnotation("http://www.example.com/annotations/0004")))

 
  val dataset0 = new DefaultDataset("http://www.example.com/datasets/0")
  dataset0.subsets = List(dataset0_0, dataset0_1, dataset0_2)
  dataset0_0.parent = Some(dataset0)
  dataset0_1.parent = Some(dataset0) 
  dataset0_2.parent = Some(dataset0)
     
  test("Verify subset hierarchy was created correctly") {
    assert(dataset0.subsets.size == 3)
    
    dataset0.subsets.foreach(_ match {
      case subset: Dataset if (subset.uri.endsWith("0_0")) => assert(subset.subsets.size == 1)
      case subset => assert(subset.subsets.size == 0)
    })
  }
  
  test("Verify annotation count") {
    assert(dataset0.countAnnotations() == 0)    
    assert(dataset0.countAnnotations(true) == 4)

    assert(dataset0_0.countAnnotations() == 0)
    assert(dataset0_0.countAnnotations(true) == 2)
    
    assert(dataset0_0_0.countAnnotations() == 2)
    assert(dataset0_0_0.countAnnotations(true) == 2)
    
    assert(dataset0_1.countAnnotations() == 1)
    assert(dataset0_1.countAnnotations(true) == 1)

    assert(dataset0_2.countAnnotations() == 1)
    assert(dataset0_2.countAnnotations(true) == 1)
  }
  
  test("Verify annotation retrieval") {
    assert(dataset0.annotations().size == 0)    
    assert(dataset0.annotations(true).size == 4)
    println("dataset0 (nested):")
    dataset0.annotations(true).foreach(a => println(a.uri))

    assert(dataset0_0.annotations().size == 0)
    assert(dataset0_0.annotations(true).size == 2)
    println("\ndataset0_0 (nested):")
    dataset0_0.annotations(true).foreach(a => println(a.uri))
    
    assert(dataset0_0_0.annotations().size == 2)
    assert(dataset0_0_0.annotations(true).size == 2)
    println("\ndataset0_0_0:")
    dataset0_0_0.annotations().foreach(a => println(a.uri))
    
    assert(dataset0_1.annotations().size == 1)
    assert(dataset0_1.annotations(true).size == 1)
    println("\ndataset0_1:")
    dataset0_1.annotations().foreach(a => println(a.uri))

    assert(dataset0_2.annotations().size == 1)
    assert(dataset0_2.annotations(true).size == 1)
    println("\ndataset0_2:")
    dataset0_2.annotations().foreach(a => println(a.uri))
  }
  
  test("Verify isChild() functionality") {
    // Must yield true
    assert(dataset0_0_0.isChildOf(dataset0_0.uri))
    assert(dataset0_0_0.isChildOf(dataset0.uri))
    assert(dataset0_1.isChildOf(dataset0.uri))
    assert(dataset0_2.isChildOf(dataset0.uri))
    
    // Must yield false
    assert(!dataset0_1.isChildOf(dataset0_0.uri))
    assert(!dataset0_2.isChildOf(dataset0_0.uri))
    assert(!dataset0_0_0.isChildOf(dataset0_1.uri))
    assert(!dataset0_0_0.isChildOf(dataset0_2.uri))    
  }

}