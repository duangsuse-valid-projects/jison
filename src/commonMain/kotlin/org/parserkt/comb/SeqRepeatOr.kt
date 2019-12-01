package org.parserkt.comb

fun <IN, T, R> maySeq(fold: Fold<T, R>, vararg sub: Parser<IN, T>): Parser<IN, R> = runParse@ {
  val reducer = fold.reducer()
  for (parse in sub) reducer.accept(parse.tryRead(it) ?: return@runParse nParsed)
  return@runParse reducer.base
}

fun <IN, T, R> seq(fold: Fold<T, R>, vararg sub: Parser<IN, T>): PositiveParser<IN, R> = runParse@ {
  val reducer = fold.reducer()
  for ((index, parse) in sub.withIndex())
    reducer.accept(parse.tryRead(it) ?: it.pFail(": seq $index"))
  return@runParse reducer.base
}

fun <IN, T, R> repeat(fold: Fold<T, R>, sub: Parser<IN, T>): Parser<IN, R> = runParse@ {
  val reducer = fold.reducer()
  var countParsed = 0
  while (true) {
    val parsed = sub.tryRead(it) ?: break
    reducer.accept(parsed).also { ++countParsed }
  }
  return@runParse reducer.base.takeIf { countParsed != 0 }
}

fun <T, R> or(vararg sub: Parser<T, R>): Parser<T, R> = runParse@ {
  for (parse in sub) parse.tryRead(it)?.let { res -> return@runParse res }
  return@runParse null
}

fun <T, R: Any?> Parser<T, R>.toMust(failMessage: String): Parser<T, R> = {
  this(it) ?: it.pFail(failMessage)
}
fun parserFail(failMessage: String): ParserFailure<*> = { it.pFail(failMessage) }

////
fun <T, R> seq(vararg sub: Parser<T, R>): PositiveParser<T, MutableList<R>> = seq(asList(), *sub)
fun <T, R> maySeq(vararg sub: Parser<T, R>): Parser<T, MutableList<R>> = maySeq(asList(), *sub)
fun <T, R> repeat(sub: Parser<T, R>): Parser<T, List<R>> = repeat(asList(), sub)
