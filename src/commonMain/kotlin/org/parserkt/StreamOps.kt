package org.parserkt
import org.parserkt.util.*

fun <T> FiniteStream<T>.asSequence(): Sequence<T>
  = sequence { while (hasNext()) yield(next()) }

inline fun <T> FiniteStream<T>.forEach(op: Consumer<T>)
{ for (item in this) op(item) }

fun <T> FiniteStream<T>.toList(): List<T>
  = mutableListOf<T>().also { this.forEach(it::justAdd) }

fun <T, ACC> FiniteStream<T>.fold(initial: ACC, op: FoldLeft<T, ACC>): ACC {
  var base = initial
  forEach { base = op(base, it) }
  return base
}
fun <T, R> FiniteStream<T>.foldRight(initial: R, op: FoldRight<T, R>): R {
  if (isEnd) return initial
  return op(next(), foldRight(initial, op))
}

// sequence { for (item in this@filter) if (predicate(item)) yield(item) }
fun <T: Any> FiniteStream<T>.filter(predicate: Predicate<T>): FiniteStream<T> = object: FiniteStream<T> {
  private val stream = this@filter
  private var readyItem: T? = null //match found
  override val isEnd: Boolean get() {
    if (stream.isEnd) return true
    do { readyItem = stream.next() }
      while (!stream.isEnd && !predicate(readyItem!!))
    return stream.isEnd && readyItem == null
  }
  override fun next(): T = readyItem!!.also { readyItem = null }
}

// sequence { for (item in this@map) yield(op(next())) }
fun <T, R> FiniteStream<T>.map(op: (T) -> R): FiniteStream<R> = object: FiniteStream<R> {
  private val stream = this@map
  override val isEnd: Boolean get() = stream.isEnd
  override fun next(): R = op(stream.next())
}

// sequence { while (predicate(peek)) yield(next()) }
fun <T> PeekStream<T>.takeWhile(predicate: Predicate<T>): FiniteStream<T> = object: FiniteStream<T> {
  private val stream = this@takeWhile
  override val isEnd: Boolean get() = stream.isEnd || !predicate(this@takeWhile.peek)
  override fun next(): T = stream.next()
}
fun <T> PeekStream<T>.dropWhile(predicate: Predicate<T>) { while (predicate(peek)) next() }
