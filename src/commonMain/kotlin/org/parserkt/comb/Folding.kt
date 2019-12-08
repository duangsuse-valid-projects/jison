package org.parserkt.comb

import org.parserkt.util.*

interface Reducer<in T, out R> {
  fun accept(item: T)
  fun finish(): R
}

typealias Foldr<A, C> = Fold<A, *, C>
abstract class Fold<in A, B, out C> {
  abstract val initial: B
  abstract fun join(base: B, item: A): B
  abstract fun finish(accumulator: B): C

  open fun reducer(): Reducer<A, C> = object: Reducer<A, C> {
    private var base: B = initial
    override fun accept(item: A) { base = join(base, item) }
    override fun finish(): C = finish(base)
  }
}

abstract class Monoid<T>(mzero: T, private val mplus: T.(T) -> T): Fold<T, T, T>() {
  override val initial: T = mzero
  final override fun join(base: T, item: T): T = base.mplus(item)
  final override fun finish(accumulator: T): T = accumulator
}
abstract class Effect<in A, B, out C>: Fold<A, B, C>() {
  abstract val acceptor: B.(A) -> Unit
  final override fun join(base: B, item: A): B = base.also { base.acceptor(item) }
}

typealias AsList<T> = Effect<T, MutableList<T>, List<T>>
fun <T> asList(): AsList<T> = object: AsList<T>() {
  override val initial: MutableList<T> get() = mutableListOf()
  override val acceptor: MutableList<T>.(T) -> Unit = { add(it) }
  override fun finish(accumulator: MutableList<T>): List<T> = accumulator
}

typealias PartialListCounter<T> = Pair<Cnt, MutableList<T>>
typealias CountedAsList<T> = Fold<T, PartialListCounter<T>, List<T>>
fun <T> partialList(vararg indices: Idx): CountedAsList<T> = object: CountedAsList<T>() {
  override val initial: PartialListCounter<T> get() = Pair(0, mutableListOf())
  override fun join(base: PartialListCounter<T>, item: T): PartialListCounter<T> {
    val scanNext = Pair(base.first.inc(), base.second)
    if (base.first in indices) base.second.add(item)
    return scanNext
  }
  override fun finish(accumulator: PartialListCounter<T>): List<T> = accumulator.second
}

typealias BoxCounter<T> = Pair<Cnt, Box<T?>>
typealias CountedAsSelect<T> = Fold<T, BoxCounter<T>, Box<T>>
fun <T> selecting(index: Idx): CountedAsSelect<T> = object: CountedAsSelect<T>() {
  override val initial: BoxCounter<T> get() = Pair(0, Box(null as T?))
  override fun join(base: BoxCounter<T>, item: T): BoxCounter<T> {
    val scanNext = Pair(base.first.inc(), base.second)
    if (base.first == index)
      base.second.item = item
    return scanNext
  }
  override fun finish(accumulator: BoxCounter<T>): Box<T> = accumulator.second.ensure()
}

typealias CharsAsStr = Fold<Char, StringBuilder, String>
fun buildStr(): CharsAsStr = object: CharsAsStr() {
  override val initial: StringBuilder get() = StringBuilder()
  override fun join(base: StringBuilder, item: Char): StringBuilder = base.append(item)
  override fun finish(accumulator: StringBuilder): String = accumulator.toString()
}
