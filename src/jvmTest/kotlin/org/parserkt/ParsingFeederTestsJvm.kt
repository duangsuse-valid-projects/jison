package org.parserkt

import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class ParsingFeederTestsJvm {
  @Test fun inputStreamCharIterator() {
    val s = parsingFeeder(ByteArrayInputStream("hello 世界".toByteArray()), file = "niHao.txt")
    assertEquals('h', s.consume())
    assertEquals('e', s.consume())
    assertEquals("llo 世", s.takeWhile { it != '界' }.joinToString(""))
  }
  @Test fun highSurrogateCodePoint() {
    val s = parsingFeeder(ByteArrayInputStream("\uD83D\uDE06 \uD83D\uDE07".toByteArray()), file = "emotions.txt")
    assertEquals("😆", listOf(s.consume(), s.consume()).joinToString(""))
    assertEquals(' ', s.consume())
    assertEquals("😇", listOf(s.consume(), s.consume()).joinToString(""))
  }
}