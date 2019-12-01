package comb

import feederOf
import org.parserkt.comb.items
import org.parserkt.comb.or
import kotlin.test.Test
import kotlin.test.assertEquals

class OrParserTests {
  private val animals = or(items("rabbit"), items("lion"), items("penguin"))
  @Test fun completeness() {
    for (kind in setOf("rabbit", "penguin", "lion"))
      oneKind(kind)
  }
  fun oneKind(name: String) {
    val f = feederOf(*name.toList().toTypedArray())
    val res = animals(f)!!
    assertEquals(name, res.joinToString(""))
  }
}
