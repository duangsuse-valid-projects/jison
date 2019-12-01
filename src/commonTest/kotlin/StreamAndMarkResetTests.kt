import org.parserkt.*
import org.parserkt.util.positional

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

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
      }
      for (char in listOf("def", "g")) assertEquals(char, mps.next())
    }
  }

  private val exampleSlice = listOf("rabbit", "banana", "grape", "melon").let(::ListSlice)
  @Test fun forEach() {
    val got = mutableListOf<String>()
    exampleSlice.stream().forEach { got.add(it) }
    for(i in exampleSlice.indices) assertEquals(exampleSlice[i], got[i])
  }
  private val exampleToList = ListSlice(listOf(1,2,3))
  @Test fun toList() {
    assertEquals(listOf(1,2,3), exampleToList.stream().toList())
  }
  @Test fun asSequence()
    { assertEquals(listOf("rabbit", "banana"), exampleSlice.stream().asSequence().take(2).toList()) }

  private val numberSlice = intArrayOf(1,2,3).toTypedArray().let(::ArraySlice)
  @Test fun foldLeft()
    { assertEquals(6, numberSlice.stream().fold(0) { ac, x -> ac+x }) }
  @Test fun foldRight()
    { assertEquals("MelonGrapeBananaRabbit", exampleSlice.stream()
          .foldRight(StringBuilder()) { name, r -> r.append(name.capitalize()) }.toString()) }

  @Test fun filter()
    { assertEquals(listOf("banana", "melon"), exampleSlice.stream()
          .filter { it.contains('n') }.toList()) }
  private val translateDict = """
    |兔子 rabbit
    |香蕉 banana
    |葡萄 grape
    |瓜 melon
  """.trimMargin().lineSequence().filterNot(String::isBlank)
        .map { val (a,b) = it.split(' '); Pair(b,a) }.toMap()
  @Test fun map()
    { assertEquals(listOf("兔子", "香蕉", "葡萄", "瓜"), exampleSlice.stream().map(translateDict::get).toList()) }

  @Test fun takeWhile() {
    assertEquals(3, exampleSlice.stream()
          .takeWhile { it.contains('a') }.toList().size)  }
  @Test fun dropWhile() {
    val s = exampleSlice.stream()
    s.dropWhile { it.contains('n') }
    assertEquals("rabbit", s.next())
    s.dropWhile { it.contains('n') } //banana
    assertEquals("grape", s.next())
  }
}