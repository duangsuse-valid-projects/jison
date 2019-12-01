package comb

import assertMessageEquals
import org.parserkt.comb.ParserError
import org.parserkt.comb.parsingFeeder
import org.parserkt.dropWhile
import kotlin.test.Test
import kotlin.test.assertEquals

class ParsingFeederTests {
  object NoError: Exception("nope")
  @Test fun sourceLocated() {
    val pf = parsingFeeder(file = "test.txt", str = """
      hello,
      cruel
      world.
    """.trimIndent() + "\r\nCRLF")
    assertMessageEquals<ParserError>("parser fail@test.txt:1:1 #0: nope") { pf.quake(NoError) }
    pf.dropWhile { it in "hello," }
    assertEquals('\n', pf.peek)
    pf.consume() //peek 'c'
    assertMessageEquals<ParserError>("parser fail@test.txt:2:1 #7: nope") { pf.quake(NoError) }
    pf.dropWhile { it != '.' }
    assertEquals('.', pf.peek)
    assertMessageEquals<ParserError>("parser fail@test.txt:3:6 #18: nope") { pf.quake(NoError) }
    pf.dropWhile { it != 'C' }
    assertMessageEquals<ParserError>("parser fail@test.txt:4:1 #20: nope") { pf.quake(NoError) }
  }
}