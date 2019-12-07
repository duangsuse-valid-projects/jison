package org.parserkt.comb

import org.parserkt.*
import org.parserkt.util.Cnt
import org.parserkt.util.Predicate

inline fun <T> satisfy(crossinline predicate: Predicate<T>): Parser<T, T>
  = { s -> s.peek.takeIf(predicate)?.let { s.consume() } }

fun <T> item(): Parser<T, T> = satisfy { true }
fun <T> item(x: T): Parser<T, T> = satisfy { it == x }
fun <T> element(vararg xs: T): Parser<T, T> = satisfy { it in xs }
fun element(xs: CharRange): Parser<Char, Char> = satisfy { it in xs }

fun <T> items(vararg xs: T): Parser<T, Array<out T>> = read@ { s ->
  for (x in xs) if (s.peek == x) s.consume()
    else return@read nParsed
  return@read xs
}
fun items(keyword: CharSequence): Parser<Char, Array<out Char>>
  = items(*keyword.toList().toTypedArray())
inline fun <reified T> nItem(n: Cnt): PositiveParser<T, Array<T>> = readCount@ { s ->
  if (s is BulkFeeder<T>) return@readCount s.take(n).consume().copyToArray()
  val res = arrayOfNulls<T>(n)
  for (i in 0 until n) res[i] = s.consume()
  @Suppress("UNCHECKED_CAST") //it's really ALL T!
  return@readCount (res as Array<T>)
}

fun <T> skip(ignore: Set<T>): PositiveParser<T, Unit>
  = { s -> (s as? SliceFeeder)?.dropWhileIn(ignore) ?: s.dropWhile { it in ignore } }
fun <T> skip(vararg ignore: T): PositiveParser<T, Unit>
  = skip(ignore.toSet())

fun <T> takeUntil(terminator: Set<T>): PositiveParser<T, Slice<T>>
  = { s -> (s as? SliceFeeder)?.takeUntilIn(terminator)
      ?: s.takeWhile { it !in terminator }.toList().let(::ListSlice) }
fun <T> takeUntil(vararg terminator: T): PositiveParser<T, Slice<T>>
  = takeUntil(terminator.toSet())
