import org.parserkt.*
import org.parserkt.util.MarkReset
import org.parserkt.util.positional
import kotlin.test.*

class FeederTests {
  private val sf = feederOf(1,2,3,4,5,6,1,3)
  @Test fun sliceFeeder() {
    assertTrue(sf is MarkReset); assertTrue(sf is BulkFeeder)
    assertEquals(1, sf.peek); assertEquals(1, sf.consume())
    assertEquals(2, sf.peek)
    sliceFeederTake()
    sliceFeederSeqScan()
  }
  private fun sliceFeederTake() {
    sf as SliceFeeder
    val view = sf.take(3)
    assertEquals(listOf(2,3,4), view.stream().toList())
    assertEquals(2, sf.peek); assertFails { sf.consume() }; assertFails { sf.take(1) }
    view.consume(); assertEquals(5, sf.peek); view.ignore() //won't fail
  }
  private fun sliceFeederSeqScan() {
    sf as SliceFeeder
    val not13 = sf.takeUntilIn(setOf(1,3))
    assertEquals(listOf(5,6), not13.stream().toList())
    sf.dropWhileIn(setOf(1))
    assertEquals(3, sf.peek)
  }

  private val lf = StreamFeederLookahead1(listOf(6,5,4).listIterator())
  @Test fun streamLL1() {
    for (_i in 1..3) assertEquals(6, lf.peek)
    for (n in listOf(6,5,4)) {
      assertEquals(n, lf.peek)
      assertEquals(n, lf.consume())
    }
    for (_i in 1..3) assertFailsWith<FiniteStream.StreamEnd> { lf.consume() }
  }

  private val mrf = MarkResetPeekStream("ni hao".toList().listIterator())
  @Test fun stream() {
    for (_i in 1..3) assertEquals('n', mrf.peek)
    mrf.next() //'i'
    mrf.positional {
      for (c in listOf('i', ' ')) assertEquals(c, mrf.next())
      mrf.positional {
        for (c in listOf('h', 'a', 'o'))
          assertEquals(c, mrf.next())
        assertTrue(mrf.isEnd)
      }
      assertFalse(mrf.isEnd)
    }
    assertEquals('i', mrf.peek)
    assertFalse(mrf.isEnd)
  }
}