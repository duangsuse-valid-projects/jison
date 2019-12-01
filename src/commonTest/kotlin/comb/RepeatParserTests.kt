package comb

import feederOf
import org.parserkt.comb.item
import org.parserkt.comb.repeat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RepeatParserTests {
  private val chars = repeat(item<Char>())
  @Test fun reads() {
    assertEquals("abc".toList(), chars(feederOf('a', 'b', 'c')))
  }
  @Test fun isPartially() {
    assertNull(chars(feederOf()))
  }
}