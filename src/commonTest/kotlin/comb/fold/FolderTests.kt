package comb.fold

import org.parserkt.comb.Reducer
import org.parserkt.util.Box
import kotlin.test.Test
import kotlin.test.assertEquals

class FolderTests {
  @Test fun asList() {
    val fr = org.parserkt.comb.asList<Int>()
    val list = listOf(1,2,3,9)
    assertReduction(list, fr.reducer(), list)
  }
  @Test fun partialList() {
    val fr = org.parserkt.comb.partialList<Int>(2,4)
    val list = listOf(1,2,3,9,0)
    assertReduction(listOf(list[2], list[4]), fr.reducer(), list)
  }
  @Test fun selecting() {
    val fr = org.parserkt.comb.selecting<Int>(1)
    val list = listOf(2,9,1)
    assertReduction(Box(9), fr.reducer(), list)
    assertReduction(Box(null), fr.reducer(), listOf(0))
  }
  @Test fun buildStr() {
    val fr = org.parserkt.comb.buildStr()
    val abc = "I can say the ABCs"
    val list = abc.toList()
    for (_i in 1..2) assertReduction(abc, fr.reducer(), list, StringBuilder::toString)
  }

  private inline fun <T, reified TT> identity(x: T): TT = x as TT //emm
  private inline fun <T, R, reified R1> assertReduction(
    expected: R1, reducer: Reducer<T, R>,
    inputs: List<T>, f: (R) -> R1 = ::identity)
  { inputs.forEach(reducer::accept)
    assertEquals(expected, reducer.finish().let(f)) }
}
