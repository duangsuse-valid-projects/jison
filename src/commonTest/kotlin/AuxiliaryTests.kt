import org.parserkt.util.*

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class AuxiliaryTests {
  private lateinit var list: MutableList<Int>
  @BeforeTest fun setupList() { list = mutableListOf(1, 2, 3) }

  @Test fun removeAtBegin() { assertEquals(1, list.removeAtBegin()) }
  @Test fun removeAtEnd() { assertEquals(3, list.removeAtEnd()) }
  @Test fun addOrderNote() { list.add(0); assertEquals(listOf(1,2,3,0), list) }
  @Test fun subList() { assertEquals(list, list.subList(list.indices)) }

  @Test fun impossibleFails() { assertFails(::impossible) }
  @Test fun boxCoercion() {
    val box = Box<Any>(0L)
    assertEquals(0L, box.force())
  }
}