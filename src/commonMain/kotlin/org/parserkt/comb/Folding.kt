package org.parserkt.comb

import org.parserkt.util.Box
import org.parserkt.util.Idx

// FIXME Reducer lifetime weaken

interface Reducer<in T, out R> {
  val base: R
  fun accept(item: T)
  fun finish(): R = base
}

abstract class Fold<in A, B> {
  abstract val initial: B
  abstract fun join(base: B, item: A): B

  open fun reducer(): Reducer<A, B> = object: Reducer<A, B> {
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
  open fun reset() {}

  override fun reducer(): Reducer<A, B> = object: Reducer<A, B> {
    override var base: B = initial
    override fun accept(item: A) { base = join(base, item) }
    override fun finish(): B = base.also { reset() }
  }
}

fun <T> asList(): Effect<T, MutableList<T>> = object: Effect<T, MutableList<T>>() {
  override val initial: MutableList<T> get() = mutableListOf()
  override val acceptor: MutableList<T>.(T) -> Unit = { add(it) }
}

fun <T> partialList(vararg indices: Idx): Effect<T, MutableList<T>> = object: Effect<T, MutableList<T>>() {
  private var position = 0
  override fun reset() { position = 0 }
  override val initial: MutableList<T> get() = mutableListOf()
  override val acceptor: MutableList<T>.(T) -> Unit = { if (position in indices) add(it); ++position }
}

fun <T> selecting(index: Idx): Effect<T, Box<T?>> = object: Effect<T, Box<T?>>() {
  private var position = 0
  override fun reset() { position = 0 }
  override val initial: Box<T?> get() = Box(null)
  override val acceptor: Box<T?>.(T) -> Unit = { if (position == index) item = it; ++position }
}

fun buildStr(): Fold<Char, StringBuilder> = object: Fold<Char, StringBuilder>() {
  override val initial: StringBuilder get() = StringBuilder()
  override fun join(base: StringBuilder, item: Char): StringBuilder = base.append(item)
}
