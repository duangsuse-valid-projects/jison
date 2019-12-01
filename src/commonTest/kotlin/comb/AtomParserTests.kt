package comb

import feederOf
import org.parserkt.comb.*
import org.parserkt.toList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AtomParserTests {
  @Test fun satisfyAndItem() {
    val p = item(1)
    assertNotNull(p(feederOf(1)))
    assertNull(p(feederOf(2)))
    val f = feederOf(1,2,3)
    p(f); assertEquals(2, f.peek)
  }
  @Test fun element() {
    val p = element('a', 'b', 'c')
    for (item in setOf('b', 'c')) assertNotNull(p(feederOf(item)))
    assertNull(p(feederOf('d')))
  }
  @Test fun elementCharRange() {
    assertNotNull(element('0'..'9')(feederOf('2')))
  }
  @Test fun items() {
    val f = feederOf(1,2,3)
    assertNotNull(items(1,2)(f))
    assertNotNull(items(*arrayOf(3))(f))
  }
  @Test fun skip() {
    val f = feederOf(1,2,5,6, 14)
    skip(6,5,2,1)(f)
    assertEquals(14, f.peek)
  }
  @Test fun takeUntil() {
    val f = feederOf(0,1,2,3,10,15)
    assertEquals(listOf(0,1,2,3), takeUntil(10)(f).stream().toList())
  }
}
