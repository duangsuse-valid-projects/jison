import org.parserkt.*

import kotlin.test.Test
import kotlin.test.assertEquals

class StreamOpsTests {
  private val exampleSlice = listOf("rabbit", "banana", "grape", "melon").let(::ListSlice)
  private val exampleToList = ListSlice(listOf(1,2,3))
  private val numberSlice = intArrayOf(1,2,3).toTypedArray().let(::ArraySlice)

  @Test fun asSequence()
  { assertEquals(listOf("rabbit", "banana"), exampleSlice.stream().asSequence().take(2).toList()) }
  @Test fun forEach() {
    val got = mutableListOf<String>()
    exampleSlice.stream().forEach { got.add(it) }
    for(i in exampleSlice.indices) assertEquals(exampleSlice[i], got[i])
  }
  @Test fun toList()
  { assertEquals(listOf(1,2,3), exampleToList.stream().toList()) }

  @Test fun foldLeft()
  { assertEquals(6, numberSlice.stream().fold(0) { ac, x -> ac+x }) }
  @Test
  fun foldRight()
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

  @Test
  fun takeWhile()
  { assertEquals(3, exampleSlice.stream()
    .takeWhile { it.contains('a') }.toList().size)  }
  @Test fun dropWhile() {
    val s = exampleSlice.stream()
    s.dropWhile { it.contains('n') }
    assertEquals("rabbit", s.next())
    s.dropWhile { it.contains('n') } //banana
    assertEquals("grape", s.next())
  }
}