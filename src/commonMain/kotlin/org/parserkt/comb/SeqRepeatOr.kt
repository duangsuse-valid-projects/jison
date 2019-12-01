package org.parserkt.comb

fun <T, R> maySeq(vararg sub: Parser<T, R>): Parser<T, List<R>> = runParse@ {
  val result = mutableListOf<R>()
  for (parse in sub) result.add(parse.tryRead(it) ?: return@runParse nParsed)
  return@runParse result
}

fun <T, R> seq(vararg sub: Parser<T, R>): PositiveParser<T, List<R>> = runParse@ {
  val result = mutableListOf<R>()
  for ((index, parse) in sub.withIndex())
    result.add(parse.tryRead(it) ?: pFail(": seq $index"))
  return@runParse result
}

fun <T, R> repeat(sub: Parser<T, R>): Parser<T, List<R>> = runParse@ {
  val result = mutableListOf<R>()
  while (true) {
    val parsed = sub.tryRead(it) ?: break
    result.add(parsed)
  }
  return@runParse result.takeIf { r -> r.isNotEmpty() }
}

fun <T, R> or(vararg sub: Parser<T, R>): Parser<T, R> = runParse@ {
  for (parse in sub) parse.tryRead(it)?.let { res -> return@runParse res }
  return@runParse null
}

fun <T, R: Any?> Parser<T, R>.toMust(failMessage: String): Parser<T, R> = {
  this(it) ?: pFail(failMessage)
}
