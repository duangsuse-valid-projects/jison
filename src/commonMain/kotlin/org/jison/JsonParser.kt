package org.jison

import org.jison.Json.Num
import org.parserkt.comb.*

interface CombinedParser<R>
  { val file: Parser<Char, R> }

sealed class Json {
  data class Dict(val map: Map<String, Json>): Json()
  data class Ary(val xs: List<Json>): Json()
  data class Str(val literal: String): Json()
  data class Num(val i: Double): Json()
  data class Bool(val p: Boolean): Json()
  object Nil: Json()
}

abstract class Lexer {
  val boolean = or(items("true") const Json.Bool(true), items("false") const Json.Bool(false))
  val nullLit = items("null" ) const Json.Nil

  val white = element(' ', '\n', '\r', '\t')
  val ws = repeat(white)

  val tCOLON = item(':')
  val tSEMI = item(',')
  val tLB = item('{'); val tRB = item('}')
  val tLS = item('['); val tRS = item(']')
  val tMINUS = item('-')

  class DecimalRead(mzero: Int): Monoid<Int>(mzero, { i -> this*10 + i })
  object HexRead: Monoid<Int>(0, { i -> this*16 + i })
  val oneNine = element('1'..'9') then { it-'0' }
  val digit = or(oneNine, item('0') const 0)
  fun digitsCtx(zero: Int) = repeat(DecimalRead(zero), digit, 0..Int.MAX_VALUE)
  val digits = digitsCtx(0)
  val hex = or(digit, element('A'..'F') then { it-'A'+10 }, element('a'..'f') then { it-'a'+10 })

  val translateMap = mapOf(
    '"' to '"',
    '\\' to '\\',
    '/' to '/',
    'b' to '\b',
    'f' to '\u0012', 't' to '\t',
    'n' to '\n', 'r' to '\r'
  )
  val escape = element('"', '\\', '/', 'b', 'f', 'n', 'r', 't') then { translateMap[it] }
  val unicodeEscapeVal = seq(HexRead, hex, hex, hex, hex)
  val specialChar = or(seq(selecting(1), items("\\u"), unicodeEscapeVal) then { (it.get() as Int).toChar() },
    seq(selecting(1), item('\\'), escape) then { it.get() })
  val character = or(specialChar, element('\u0020'..Char.MAX_LOW_SURROGATE))
  val string = repeatUntil(asString(), character, item('"')) then { Json.Str(it.toString()) }

  val coefficientMap = mapOf('+' to 1, '-' to -1)
  val digitsNoLeadingZero = or(digit, oneNine contextual { digitsCtx(it) })
  val sign = element('+', '-')
  val integer = seq(tMINUS.toParsedPosNeg() then { if (it) -1 else 1 }, digitsNoLeadingZero) then { it[0]*it[1] }
  val fraction = seq(selecting(1), item('.'), digits) then { it.get() as Int }
  val exponents = seq(partialList(1,2), element('E', 'e'), sign.toDefault('+'), digits) then { coefficientMap[it[1]]!!*(it[2] as Int) }
  val number = seq(integer, fraction.toDefault(0), exponents.toDefault(0)) then { "${it[0]}.${it[1]}E${it[2]}".toDouble().let(::Num) }
}

object JsonParser: CombinedParser<Json>, Lexer() {
  val value: Parser<Char, Json> by lazy(LazyThreadSafetyMode.NONE) {
    or(jsonObj, array, string, number, boolean, nullLit) }
  val element = seq(selecting(1), ws, value, ws) then { it.get() as Json }
  val kvPair = seq(partialList(1,4), ws, string, ws, tCOLON, element) then { Pair(it[1] as String, it[4] as Json) }
  val jsonObj = kvPair.joinBy(tSEMI).then { it.toMap() }.surround(tLB, tRB) then { Json.Dict(it) }
  val arrayElement = seq(selecting(1), ws, value, ws) then { it.get() as Json }
  val array = arrayElement.joinBy(tSEMI).surround(tLS, tRS) then { Json.Ary(it) }
  override val file: Parser<Char, Json> = element
}

@Suppress("UNCHECKED_CAST")
fun <T, R> Parser<T, R>.joinBy(join: Parser<T, *>): Parser<T, List<R>>
  = repeat(seq(selecting(0), this, join) then { it.get() as R })
@Suppress("UNCHECKED_CAST")
fun <T, R> Parser<T, R>.surround(l: Parser<T, *>, r: Parser<T, *>): Parser<T, R>
  = seq(selecting(1), l, this, r) then { it.get() as R }
