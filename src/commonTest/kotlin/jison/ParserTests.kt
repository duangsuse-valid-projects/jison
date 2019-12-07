package jison

import feederOf
import org.jison.Json
import org.jison.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTests {
  @Test fun root() {
    assertEquals(Json.Bool(true), JsonParser.scalar(feederOf("true")))
    assertEquals(Json.Nil, JsonParser.scalar(feederOf("null")))
    assertEquals(Json.Str("e"), JsonParser.scalar(feederOf("\"e\"")))
    assertEquals(Json.Num(-23.0), JsonParser.scalar(feederOf("-23")))
  }
  @Test fun element() {
    assertEquals(Json.Bool(true), JsonParser.element(feederOf("  true\n")))
    assertEquals(Json.Bool(false), JsonParser.element(feederOf("\rfalse")))
  }
  @Test fun kvPair() {
    assertEquals(Pair("hello", Json.Str("world")), JsonParser.kvPair(feederOf(" \"hello\" : \"world\"")))
  }
  @Test fun map() {
    val example = mapOf("name" to Json.Str("suz"),
      "age" to Json.Num(17.9),
      "boy" to Json.Bool(true),
      "extra" to Json.Ary(listOf()))
    val jsObject = """
      {
        "name": "suz",
        "age": 17.9,
        "boy": true,
        "extra": []
      }
    """.trimIndent()
    assertEquals(Json.Dict(example), JsonParser.element(feederOf(jsObject)))
  }
}