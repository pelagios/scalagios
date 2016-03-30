package org.pelagios.api

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.slf4j.LoggerFactory

@RunWith(classOf[JUnitRunner])
class PeriodOfTimeTest extends FunSuite {
  
  val logger = LoggerFactory.getLogger(classOf[PeriodOfTimeTest])
    
  test("PeriodOfTime") {
    val periodOfTime = PeriodOfTime.fromString("-30/640")
    logger.info(periodOfTime.start.toString)
    logger.info(periodOfTime.end.get.toString)
    assert(periodOfTime.start != null)
  }
  
}
