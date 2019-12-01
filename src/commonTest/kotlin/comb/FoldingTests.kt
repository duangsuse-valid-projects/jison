package comb

import org.parserkt.comb.Fold
import org.parserkt.comb.Monoid
import org.parserkt.comb.asList
import kotlin.test.Test
import kotlin.test.assertEquals

class FoldingTests {
  @Test fun completeness() {
    val reducer = asList<Int>().reducer()
    for (i in listOf(1,2,3)) reducer.accept(i)
    assertEquals(listOf(1,2,3), reducer.base)
  }
  object Sum: Monoid<Int>(0, Int::plus)
  @Test fun monoidFold() {
    val reducer = Sum.reducer()
    for (i in setOf(1,2,3)) reducer.accept(i)
    assertEquals(1+2+3, reducer.base)
  }
  object DecimalRead: Fold<Char, Int>() {
    override val initial = 0
    override fun join(base: Int, item: Char): Int = base*10 + (item-'0')
  }
  @Test fun complexFold() {
    val reducer = DecimalRead.reducer()
    "12345".forEach(reducer::accept)
    assertEquals(12345, reducer.base)
  }
}