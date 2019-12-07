package comb

import feederOf
import org.parserkt.comb.anyItem
import org.parserkt.comb.repeat1
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RepeatParserTests {
  private val chars = repeat1(anyItem<Char>())
  @Test fun reads() {
    assertEquals("abc".toList(), chars(feederOf('a', 'b', 'c')))
  }
  @Test fun isPartially() {
    assertNull(chars(feederOf()))
  }
}