package org.parserkt.comb

import org.parserkt.util.Cnt

fun <IN, T, R> seq(fold: Foldr<T, R>, vararg sub: Parser<IN, T>): Parser<IN, R> = runParse@ {
  val reducer = fold.reducer()
  for (parse in sub)
    parse.tryRead(it)?.let(reducer::accept)
      ?: return@runParse nParsed
  return@runParse reducer.finish()
}

fun <IN, T, R> mustSeq(fold: Foldr<T, R>, vararg sub: Parser<IN, T>): PositiveParser<IN, R> = runParse@ {
  val reducer = fold.reducer()
  for ((index, parse) in sub.withIndex())
    parse.tryRead(it)?.let(reducer::accept)
      ?: it.pFail(": seq $index")
  return@runParse reducer.finish()
}

fun <IN, T, R> repeat(fold: Foldr<T, R>, item: Parser<IN, T>, bound: IntRange): Parser<IN, R> = runParse@ {
  val reducer = fold.reducer()
  var countParsed: Cnt = 0
  while (true) {
    val parsed = item.tryRead(it) ?: break
    reducer.accept(parsed).also { ++countParsed }
  }
  return@runParse reducer.finish().takeIf { countParsed in bound }
}
val SOME = 1..Int.MAX_VALUE
val MAYBE = 0..Int.MAX_VALUE

fun <IN, T, R> repeatUntil(fold: Foldr<T, R>, item: Parser<IN, T>, terminate: Parser<IN, *>): Parser<IN, R> = runParse@ {
  val reducer = fold.reducer()
  while (terminate.tryRead(it) == nParsed) {
    val parsed = item.tryRead(it) ?: break
    reducer.accept(parsed)
  }
  return@runParse reducer.finish()
}

fun <T, R> or(vararg sub: Parser<T, R>): Parser<T, R> = runParse@ { feeder ->
  for (parse in sub)
    parse.tryRead(feeder)?.let { return@runParse it }
  return@runParse nParsed
}

////
fun <T, R> mustSeq(vararg sub: Parser<T, R>): PositiveParser<T, List<R>> = mustSeq(asList(), *sub)
fun <T, R> seq(vararg sub: Parser<T, R>): Parser<T, List<R>> = seq(asList(), *sub)
fun <T, R> repeat(sub: Parser<T, R>, n: IntRange = MAYBE): Parser<T, List<R>> = repeat(asList(), sub, n)
fun <T, R> repeat1(sub: Parser<T, R>): Parser<T, List<R>> = repeat(sub, SOME)
