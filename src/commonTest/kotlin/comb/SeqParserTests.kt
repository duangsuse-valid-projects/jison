package comb

import assertMessageEquals
import feederOf
import org.parserkt.comb.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SeqParserTests {
  private val numbers = mustSeq(items(1,2,3), items(4,5,6), items(1))
  @Test fun seq() {
    val res = numbers(feederOf(1,2,3,4,5,6,7))
    assertEquals(listOf(1,2,3), res[0].toList())
    assertEquals(listOf(4,5,6), res[1].toList())
    assertEquals(listOf(7), res[2].toList())
  }
  @Test fun seqSoundness() {
    assertMessageEquals<ParserError>("parser fail: seq 1") { numbers(feederOf(1,2,3,4)) }
  }
  private val oneTwo = seq(item(1), item())
  @Test fun maySeq() {
    assertNotNull(oneTwo(feederOf(1,2)))
    assertNull(oneTwo(feederOf()))
    val res = oneTwo(feederOf(1,9))!!
    assertEquals(9, res[1])
  }
}
