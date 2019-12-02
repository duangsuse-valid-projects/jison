import org.parserkt.*

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SliceAndSizedTests {
  private val listSlice = ListSlice(listOf(1, 2, 3))
  private val arraySlice = ArraySlice(arrayOf(1, 2, 3))
  private val charSlice = CharSlice("abc")

  private val emptySized = ListSlice(emptyList<Unit>())

  @Test fun emptiness() {
    assertTF(listSlice) { Pair(isNotEmpty, isEmpty) }
    assertTF(emptySized) { Pair(isEmpty, isNotEmpty) }
  }
  @Test fun indices() { //and lastIndex
    assertEquals(0..2, listSlice.indices)
    assertEquals(0..2, arraySlice.indices)
    assertEquals(0..2, charSlice.indices)
  }
  @Test fun coerceInbound() {
    assertEquals(0, listSlice.coerceInbound(-1))
    assertEquals(2, listSlice.coerceInbound(9))
    assertEquals(listSlice.indices, listSlice.coerceInbound(-1..9))
  }

  @Test fun get() {
    for (i in listSlice.indices) {
      assertEquals(i+1, listSlice[i])
      assertEquals(i+1, arraySlice[i])
    }
    assertEquals('a', charSlice[0])
  }
  @Test fun subSlice() {
    for (slice in setOf(arraySlice, listSlice)) {
      assertEquals(1, slice[0..2][0])
      assertEquals(3, slice[0..2][2])
      val oneTo2 = slice[1..2].stream()
      assertEquals(2, oneTo2.next())
      assertEquals(3, oneTo2.next())
      assertTrue(oneTo2.isEnd)
    }
  }
}