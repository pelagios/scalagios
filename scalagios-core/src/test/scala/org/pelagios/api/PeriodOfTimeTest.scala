package org.pelagios.api

import java.text.SimpleDateFormat
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.slf4j.LoggerFactory

@RunWith(classOf[JUnitRunner])
class PeriodOfTimeTest extends FunSuite {
    
  test("PeriodOfTime") {
    val periodOfTime = PeriodOfTime.fromString("-20/630")
    assert(periodOfTime.start.getTime == -62798371200000l)
    assert(periodOfTime.end.get.getTime == -42286320000000l)
  }
  
}
