package org.parserkt

import org.parserkt.util.Cnt
import org.parserkt.util.Predicate

fun <T> Feeder<T>.dropWhile(predicate: Predicate<T>) {
  try { while (true)
    if (predicate(peek)) consume()
    else break
  } catch (_: FiniteStream.StreamEnd) {}
}

fun <T> Feeder<T>.takeWhile(predicate: Predicate<T>): Sequence<T> = sequence {
  try { while (true)
    if (predicate(peek)) yield(consume())
    else break
  } catch (_: FiniteStream.StreamEnd) {}
}

fun <T> Feeder<T>.takeItemN(n: Cnt): Sequence<T> {
  var count = 0
  return takeWhile { count++ != n }
}
