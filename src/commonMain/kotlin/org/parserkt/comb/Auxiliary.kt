package org.parserkt.comb

import org.parserkt.Feeder
import org.parserkt.FiniteStream
import org.parserkt.util.Consumer
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

fun <T, R: Any?> Parser<T, R>.toMust(failMessage: String): PositiveParser<T, R>
  = { this(it) ?: it.pFail(failMessage) }

fun <T> Parser<T, *>.toParsedPosNeg(): PositiveParser<T, Boolean>
  = posNeg@ { return@posNeg this(it) != nParsed }

fun <T, R> Parser<T, R>.toDefault(defaultValue: R): PositiveParser<T, R>
  = defaulting@ { return@defaulting this(it) ?: defaultValue }

fun parserFail(failMessage: String): ParserFailure<*> = { it.pFail(failMessage) }

infix fun <T, R, R1> Parser<T, R>.then(op: (R) -> R1): Parser<T, R1>
  = pipe@ { return@pipe this(it)?.let(op) }

infix fun <T, R1> Parser<T, *>.const(constant: R1): Parser<T, R1> = this then { constant }

infix fun <T, R, R1> Parser<T, R>.contextual(next: (R) -> Parser<T, R1>): Parser<T, R1>
  = ctxRewrite@ { return@ctxRewrite this(it)?.let(next)?.invoke(it) }

infix fun <T, R> Parser<T, R>.effect(op: Consumer<R>): Parser<T, R>
  = effectAlso@{ return@effectAlso this(it)?.also(op) }
