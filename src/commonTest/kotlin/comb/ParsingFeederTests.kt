package comb

import assertMessageEquals
import org.parserkt.Feeder
import org.parserkt.asSequence
import org.parserkt.comb.ParserError
import org.parserkt.comb.ParsingFeeder
import org.parserkt.comb.bulkParsingFeeder
import org.parserkt.comb.parsingFeeder
import org.parserkt.dropWhile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ParsingFeederTests {
  object NoError: Exception("nope")
  private val pf = parsingFeeder(str = """
      hello,
      cruel
      world.
    """.trimIndent() + "\r\nCRLF\r.", file = "test.txt")
  @Test fun sourceLocated() {
    fun assertMessage(expected: String): Unit = assertFailMessageEquals(expected, pf)
    assertMessage("parser fail@test.txt:1:1 #0: nope")

    pf.dropWhile { it in "hello," }
    assertEquals('\n', pf.peek); pf.consume() //peek 'c'
    assertMessage("parser fail@test.txt:2:1 #7: nope")

    pf.dropWhile { it != '.' }
    assertEquals('.', pf.peek)
    assertMessage("parser fail@test.txt:3:6 #18: nope")

    pf.dropWhile { it != 'C' }
    assertMessage("parser fail@test.txt:4:1 #20: nope")

    pf.dropWhile { it != '.' }
    assertMessage("parser fail@test.txt:5:1 #25: nope")
  }

  private val bpf = bulkParsingFeeder(str = """
    One day, a cool boy
      living in a pretty house
     falls in love with
       an ugly frog.
  """.trimIndent()+"\r"+"""
    The frog says:
  """.trimIndent()+"\r\n"+"""
    "woof! woof!!!"
    And many years later, they lived happily together.
  """.trimIndent())
  @Test fun bulkReading() {
    fun assertMessage(expected: String): Unit = assertFailMessageEquals(expected, bpf)
    assertMessage("parser fail@<anon>:1:1 #0: nope")
    bpf.take(10).ignore()
    assertMessage("parser fail@<anon>:1:1 #0: nope")
    assertEquals("One day, a", takes())
    assertMessage("parser fail@<anon>:1:10 #9: nope")
    assertEquals(" cool boy\n", takes())
    assertMessage("parser fail@<anon>:2:1 #18: nope")
    assertEquals("  living i"+"n a pretty", takes(20))
    assertMessage("parser fail@<anon>:2:20 #37: nope")
    assertEquals(" house\n fa", takes())
    assertEquals("lls in love with\n   an ugly frog.\r", takes(34))
    assertMessage("parser fail@<anon>:5:1 #79: nope")
    assertEquals("The frog says:", takes(14))
    assertEquals("\r\n\"woo", takes(1+5))
    assertMessage("parser fail@<anon>:6:4 #97: nope")
  }

  private fun takes(n: Int = 10) = bpf.take(n).consume().stream().asSequence().joinToString("")
  @Test fun brief() {
    val view = briefView(pf)
    assertEquals(10, view?.length)
    assertEquals("hello,\ncru", view)

    bpf.take(10).consume()
    val view1 = briefView(bpf)
    assertEquals(20, view1?.length)
    assertEquals("One day, a cool boy\n", view1)
  }

  private fun assertFailMessageEquals(expected: String, pf: ParsingFeeder<*>)
    = assertMessageEquals<ParserError>(expected) { pf.quake(NoError) }
  private fun briefView(feed: Feeder<*>): String?
    = assertFailsWith<ParserError> { feed.quake(NoError) }.briefView
}