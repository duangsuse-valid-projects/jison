package org.parserkt.comb

fun <IN, T, R> seq(fold: Fold<T, R>, vararg sub: Parser<IN, T>): Parser<IN, R> = runParse@ {
  val reducer = fold.reducer()
  for (parse in sub) reducer.accept(parse.tryRead(it) ?: return@runParse nParsed)
  return@runParse reducer.base
}

fun <IN, T, R> mustSeq(fold: Fold<T, R>, vararg sub: Parser<IN, T>): PositiveParser<IN, R> = runParse@ {
  val reducer = fold.reducer()
  for ((index, parse) in sub.withIndex())
    reducer.accept(parse.tryRead(it) ?: it.pFail(": seq $index"))
  return@runParse reducer.base
}

fun <IN, T, R> repeat(fold: Fold<T, R>, sub: Parser<IN, T>, n: IntRange): Parser<IN, R> = runParse@ {
  val reducer = fold.reducer()
  var countParsed = 0
  while (true) {
    val parsed = sub.tryRead(it) ?: break
    reducer.accept(parsed).also { ++countParsed }
  }
  return@runParse reducer.base.takeIf { countParsed in n }
}

fun <IN, T, R> repeatUntil(fold: Fold<T, R>, sub: Parser<IN, T>, terminate: Parser<IN, *>): Parser<IN, R> = runParse@ {
  val reducer = fold.reducer()
  while (terminate.tryRead(it) == null) {
    val parsed = sub.tryRead(it) ?: break
    reducer.accept(parsed)
  }
  return@runParse reducer.base
}

fun <T, R> or(vararg sub: Parser<T, R>): Parser<T, R> = runParse@ {
  for (parse in sub) parse.tryRead(it)?.let { res -> return@runParse res }
  return@runParse null
}

////
fun <T, R> mustSeq(vararg sub: Parser<T, R>): PositiveParser<T, MutableList<R>> = mustSeq(asList(), *sub)
fun <T, R> seq(vararg sub: Parser<T, R>): Parser<T, MutableList<R>> = seq(asList(), *sub)
fun <T, R> repeat(sub: Parser<T, R>, n: IntRange = 0..Int.MAX_VALUE): Parser<T, List<R>> = repeat(asList(), sub, n)
fun <T, R> repeat1(sub: Parser<T, R>): Parser<T, List<R>> = repeat(sub, 1..Int.MAX_VALUE)
