package jison

import feederOf
import org.jison.Json
import org.jison.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LexerTests {
  @Test fun booleanNil() {
    assertEquals(Json.Bool(true), JsonParser.boolean(feederOf("true")))
    assertEquals(Json.Bool(false), JsonParser.boolean(feederOf("false")))
    assertEquals(Json.Nil, JsonParser.nullLit(feederOf("null")))
  }
  @Test fun whites() {
    assertNotNull(JsonParser.ws(feederOf("\n  \t")))
    assertNotNull(JsonParser.ws(feederOf("")))
  }
  @Test fun decimal() {
    assertEquals(213, JsonParser.digits(feederOf("213")))
    assertEquals(43520, JsonParser.digits(feederOf("43520")))
    assertEquals(9, JsonParser.digits(feederOf("9")))
    assertEquals(1, JsonParser.digits(feederOf("01")))
  }
  @Test fun decimalNoLeadingZero() {
    assertEquals(0, JsonParser.digitsNoLeadingZero(feederOf("0")))
    assertNull(JsonParser.digitsNoLeadingZero(feederOf("02")))
  }
  @Test fun escape() {
    assertEquals('\r', JsonParser.specialChar(feederOf("\\r")))
    assertEquals('\u000C', JsonParser.specialChar(feederOf("\\u000C")))
    assertEquals('\n', JsonParser.specialChar(feederOf("\\n")))
  }
  @Test fun string() {
    assertEquals(Json.Str("hello\n\r"), JsonParser.string(feederOf("\"hello\\n\\r\"")))
    assertEquals(Json.Str("hello"), JsonParser.string(feederOf("\"hello\"")))
    assertEquals(Json.Str("hello"), JsonParser.string(feederOf("\"hello\"")))
    assertEquals(Json.Str("hello\uD83D\uDC14"), JsonParser.string(feederOf("\"hello\\uD83D\\uDC14\"")))
  }
}