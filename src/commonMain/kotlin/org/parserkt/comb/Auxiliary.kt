package org.parserkt.comb

import org.parserkt.Feeder
import org.parserkt.FiniteStream
import org.parserkt.util.MarkReset
import org.parserkt.util.positional

typealias Parser<T, R> = (Feeder<T>) -> R?
typealias PositiveParser<T, R> = (Feeder<T>) -> R
typealias NegativeParser<T> = (Feeder<T>) -> Nothing?
typealias ParserFailure<T> = (Feeder<T>) -> Nothing

class ParserError(extra: String): Exception("parser fail$extra")
fun Feeder<*>.pFail(extra: String): Nothing = quake(ParserError(extra))
inline val nParsed: Nothing? get() = null

fun <T, R> Parser<T, R>.tryRead(feeder: Feeder<T>): R? {
  fun read(): R? = try { this(feeder) } catch (_: FiniteStream.StreamEnd) { nParsed }
  return (feeder as? MarkReset)?.positional(::read) ?: read()
}
