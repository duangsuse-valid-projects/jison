import org.parserkt.*
import org.parserkt.util.positional
import org.parserkt.util.StateStackMarkReset
import kotlin.test.*

/** [SliceStream], [StateStackMarkReset] */
class SliceStreamTests {
  private val slice = ArraySlice(arrayOf(1,2,3))
  private lateinit var stream: SliceStream<Int>
  @BeforeTest fun setupStream() { stream = slice.stream() }

  @Test fun isEnd() {
    for (_i in 1..3) { assertFalse(stream.isEnd); stream.next() }
    assertTrue(stream.isEnd)
  }
  @Test fun peekAndNext() {
    assertEquals(1, stream.peek)
    assertEquals(1, stream.next())
    assertEquals(2, stream.peek)
  }
  @Test fun streamEndException() {
    for (slice in setOf(this.slice, ListSlice(listOf(1,2,3)), CharSlice("abc"))) {
      val s = slice.stream()
      for (_i in 1..2) assertFailsWith<FiniteStream.StreamEnd> {
        for (_ii in 1..4) s.next()
      }
    }
  }
  @Test fun take() {
    assertEquals(2, stream.take(2)[1])
  }
  @Test fun takeBoundCheck() {
    assertEquals(3, stream.take(3)[2])
    assertFails { stream.take(1) }
  }
  @Test fun markReset() {
    for (_i in 1..3) stream.positional {
      assertEquals(1, stream.next())
      assertEquals(2, stream.next())
      for (_ii in 1..3) stream.positional {
        assertEquals(3, stream.next())
        assertTrue(stream.isEnd)
      }
    }
  }
}