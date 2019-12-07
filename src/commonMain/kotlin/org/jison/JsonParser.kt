package org.jison

import org.jison.Json.Num
import org.parserkt.comb.*
import org.parserkt.util.force

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

val translateMap = mapOf(
  '"' to '"',
  '\\' to '\\',
  '/' to '/',
  'b' to '\b',
  'f' to '\u0012', 't' to '\t',
  'n' to '\n', 'r' to '\r'
)

typealias CParser<R> = Parser<Char, R>
abstract class Lexer {
  val boolean = or(items("true") const Json.Bool(true),
    items("false") const Json.Bool(false))
  val nullLit = items("null" ) const Json.Nil

  private val white = element(' ', '\n', '\r', '\t')
  val ws = repeat(white)

  val tCOLON = item(':')
  val tCOMMA = item(',')
  private val tQUOTE = item('"')
  val tLB = item('{'); val tRB = item('}')
  val tLS = item('['); val tRS = item(']')
  private val tMINUS = item('-')

  class DecimalRead(mzero: Int): Monoid<Int>(mzero, { i -> this*10 + i })
  object HexRead: Monoid<Int>(0, { i -> this*16 + i })

  private val oneNine = element('1'..'9') then { it-'0' }
  private val zero = item('0') const 0
  private val digit = or(oneNine, zero)
  private fun digitsCtx(zero: Int, optional: Boolean): CParser<Int>
    = repeat(DecimalRead(zero), digit, if (optional) MAYBE else SOME)
  val digits = digitsCtx(0, optional = false)
  val digitsNoLeadingZero: CParser<Int> = or(digit.single(), oneNine contextual { digitsCtx(it, optional = true) })
  private val hexDigit: CParser<Int> = or(digit, element('A'..'F') then { it-'A'+10 }, element('a'..'f') then { it-'a'+10 })

  private val escape = element(*translateMap.keys.toTypedArray()) then(translateMap::get)
  private val unicodeEscapeVal = seq(HexRead, hexDigit, hexDigit, hexDigit, hexDigit)
  private val unicodeEscape = seq(snd, items("\\u"), unicodeEscapeVal) then { it.force<Int>().toChar() }
  private val enumEscape: CParser<Char> = seq(snd, item('\\'), escape).unwrap()
  val specialChar = or(unicodeEscape, enumEscape)
  private val character = or(specialChar, anyItem()) // element('\u0020'..Char.MAX_LOW_SURROGATE)
  val string: CParser<Json.Str> = seq(snd, tQUOTE,
    repeatUntil(buildStr(), character, item('"')) then { Json.Str(it.toString()) }).unwrap()

  private val coefficientMap = mapOf('+' to 1, '-' to -1)
  private val sign = element('+', '-')
  private val integer = seq(tMINUS.toParsedPosNeg() then { if (it) -1 else 1 }, digitsNoLeadingZero) then { it[0]*it[1] }
  private val fraction: CParser<Int> = seq(snd, item('.'), digits).unwrap()
  private val exponents = seq(partialList(1,2), element('E', 'e'), sign.toDefault('+'), digits)
    .then { coefficientMap[it[0]]!!*(it[1] as Int) }
  val number: CParser<Num> = seq(integer, fraction.toDefault(0), exponents.toDefault(0))
    .then { "${it[0]}.${it[1]}E${it[2]}".toDouble().let(::Num) }
}


object JsonParser: CombinedParser<Json>, Lexer() {
  val scalar: CParser<Json> by lazy { or(jsonObj, jsonAry, string, number, boolean, nullLit) }
  internal inline val element: CParser<Json> get() = seq(snd, ws, deferred { scalar }, ws).unwrap()
  internal inline val kvPair: CParser<Pair<String, Json>> get() = seq(partialList(1,4),
    ws, string, ws, tCOLON, element) then { Pair((it[0] as Json.Str).literal, it[1] as Json) }

  private val jsonObj: CParser<Json.Dict> = kvPair.joinBy(tCOMMA)
    .then { it.toMap() }.surroundBy(tLB, tRB) then { Json.Dict(it) }
  private val jsonAry: CParser<Json.Ary> = element.joinBy(tCOMMA)
    .surroundBy(tLS, tRS) then { Json.Ary(it) }

  override val file: Parser<Char, Json> = element
}

inline fun <T, reified R> Parser<T, R>.joinBy(noinline join: Parser<T, *>): Parser<T, List<R>>
  = repeat(or(seq(selecting(1), join, this).unwrap(), this))
inline fun <T, reified R> Parser<T, R>.surroundBy(noinline l: Parser<T, *>, noinline r: Parser<T, *>): Parser<T, R>
  = seq(selecting(1), l, this, r).unwrap<T, R>()

inline fun <T, reified R> Parser<T, R>.single(): Parser<T, R> = takeSingle@ { s ->
  val res = this.tryRead(s)
  return@takeSingle res.takeIf { this.tryRead(s) == null }
}

internal inline val snd get() = selecting<Any>(1)
