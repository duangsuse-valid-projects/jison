package org.parserkt.comb

interface Reducer<in T, out R> {
  val base: R
  fun accept(item: T)
}

abstract class Fold<in A, B> {
  abstract val initial: B
  abstract fun join(base: B, item: A): B

  fun reducer(): Reducer<A, B> = object: Reducer<A, B> {
    override var base: B = initial
    override fun accept(item: A) { base = join(base, item) }
  }
}

abstract class Monoid<T>(mzero: T, private val mplus: T.(T) -> T): Fold<T, T>() {
  override val initial: T = mzero
  final override fun join(base: T, item: T): T = base.mplus(item)
}
abstract class Effect<in A, B>: Fold<A, B>() {
  abstract val acceptor: B.(A) -> Unit
  final override fun join(base: B, item: A): B = base.also { base.acceptor(item) }
}

fun <T> asList(): Effect<T, MutableList<T>> = object: Effect<T, MutableList<T>>() {
  override val initial: MutableList<T> get() = mutableListOf()
  override val acceptor: MutableList<T>.(T) -> Unit = { add(it) }
}
