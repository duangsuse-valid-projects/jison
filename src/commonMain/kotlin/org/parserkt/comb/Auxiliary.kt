package org.parserkt.comb

import org.parserkt.Feeder
import org.parserkt.BulkFeeder
import org.parserkt.FiniteStream
import org.parserkt.util.*

typealias Parser<T, R> = (Feeder<T>) -> R?
typealias BulkParser<T, R> = (BulkFeeder<T>) -> R?
typealias PositiveParser<T, R> = (Feeder<T>) -> R
typealias NegativeParser<T> = (Feeder<T>) -> Nothing?
typealias ParserFailure<T> = (Feeder<T>) -> Nothing

class ParserError(extra: String, val briefView: String? = null): Exception("parser fail$extra")
fun Feeder<*>.pFail(extra: String): Nothing = quake(ParserError(extra))
inline val nParsed: Nothing? get() = null

/** Read from [feeder], ignore [FiniteStream.StreamEnd] ([nParsed]), [positional] if [MarkReset]-able */
fun <T, R> Parser<T, R>.tryRead(feeder: Feeder<T>): R? {
  fun read(): R? = try { this(feeder) }
    catch (_: FiniteStream.StreamEnd) { nParsed }
  return if (feeder is MarkReset) { feeder.mark()
    read()?.also { feeder.unmark() }
      ?: nParsed.also { feeder.reset() }
  } else read()
}

fun <T, R: Any?> Parser<T, R>.toMust(failMessage: String): PositiveParser<T, R>
  = must@ { return@must this(it) ?: it.pFail(failMessage) }

fun <T> Parser<T, *>.toParsedPosNeg(): PositiveParser<T, Boolean>
  = posNeg@ { return@posNeg this(it) != nParsed }

fun <T, R> Parser<T, R>.toDefault(defaultValue: R): PositiveParser<T, R>
  = defaulting@ { return@defaulting this(it) ?: defaultValue }

fun parserFail(failMessage: String): ParserFailure<*> = { it.pFail(failMessage) }

inline infix fun <T, R, R1> Parser<T, R>.then(crossinline op: (R) -> R1): Parser<T, R1>
  = pipe@ { return@pipe this(it)?.let(op) }
infix fun <T, R1> Parser<T, *>.const(constant: R1): Parser<T, R1>
  = this then { constant }
inline fun <T, reified R1> Parser<T, Box<*>>.unwrap(): Parser<T, R1>
  = this then { it.force<R1>() }

/** Contextual parser: construct target parser dynamically via [next] */
inline infix fun <T, R, R1> Parser<T, R>.contextual(crossinline next: (R) -> Parser<T, R1>): Parser<T, R1>
  = ctxRewrite@ { return@ctxRewrite this(it)?.let(next)?.invoke(it) }

/** Side-effect [op] when parser success */
inline infix fun <T, R> Parser<T, R>.effect(crossinline op: Consumer<R>): Parser<T, R>
  = effectAlso@ { return@effectAlso this(it)?.also(op) }

inline infix fun <T, R> Parser<T, R>.pre(crossinline op: Operation): Parser<T, R>
  = effectBefore@ { op(); return@effectBefore this(it) }

fun <T, R> deferred(ps: Producer<Parser<T, R>>): Parser<T, R> = { ps()(it) }
