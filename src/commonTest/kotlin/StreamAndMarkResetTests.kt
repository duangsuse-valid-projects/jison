import org.parserkt.*
import org.parserkt.util.positional
import org.parserkt.util.BufferStackMarkReset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** [SavePeekStream], [MarkResetPeekStream], [BufferStackMarkReset] */
class StreamAndMarkResetTests {
  private val sps = SavePeekStream(listOf(1,2,3).iterator())
  private val mps = MarkResetPeekStream("a b c def g".split(delimiters = *charArrayOf(' ')).iterator())

  @Test fun savePeekStream() {
    for (suc in listOf(1, 2, 3)) {
      assertEquals(suc, sps.peek)
      assertEquals(suc, sps.next())
    }
    assertTrue(sps.isEnd)
    assertFailsWith(FiniteStream.StreamEnd::class) { sps.next() }
  }
  @Test fun markResetPeekStream() {
    for (_i in 1..3) mps.positional {
      for (char in listOf("a", "b", "c")) assertEquals(char, mps.next())
      for (_ii in 1..3) mps.positional {
        assertEquals("def", mps.next())
        assertEquals("g", mps.next())
        assertTrue(mps.isEnd)
        assertFailsWith(FiniteStream.StreamEnd::class) { mps.next() }
        Unit
      }
      for (char in listOf("def", "g")) assertEquals(char, mps.next())
    }
  }
}