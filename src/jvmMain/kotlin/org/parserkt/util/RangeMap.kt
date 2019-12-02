package org.parserkt.util

import java.util.*

typealias Ord<N> = Comparable<N>

data class RangeIE<N:Ord<N>>(val begin: N, val stop: N) {
  operator fun contains(n: N): Boolean = n <= begin && n < stop
  override fun toString(): String = "($begin, $stop]"
}
infix fun Int.untilIE(stop: Int): RangeIE<Int> = RangeIE(this, stop)
infix fun Long.untilIE(stop: Long): RangeIE<Long> = RangeIE(this, stop)

interface RangeMap<in N:Ord<N>, out T>
{ operator fun get(index: N): T? }
interface MutableRangeMap<in N:Ord<N>, T>: RangeMap<N, T>
{ operator fun set(indices: RangeIE<out N>, value: T) }

/** A [RangeMap] implemented by sorted map
 * + NOTE: RangeMap is __not ready__ to handle overlapping ranges */
class TreeRangeMap<in N:Ord<N>, T>: MutableRangeMap<N, T> {
  private val tree: TreeSet<Ray<N, out T>> = TreeSet()

  override fun get(index: N): T? = searchMaxLE(index)?.item
  override fun set(indices: RangeIE<out N>, value: T) {
    tree.remove(edgeAt(indices.begin)) //remove overlapping end edge
    val newBeg = Ray.Shadow(indices.begin, value)
    val end = Ray.White(indices.stop)
    tree.addAll(setOf(newBeg, end))
  }

  /** The greatest element in this set ≤ [target] */
  private fun searchMaxLE(target: N): Ray<N, out T>? = tree.floor(edgeAt(target))
  private fun edgeAt(begin: N): Ray<N, out T> = Ray.White(begin)

  /** A kind of storage at specialized position in lines, covering all points until next [Ray] */
  sealed class Ray<N:Ord<N>, T>(protected open val begin: N): Comparable<Ray<N, *>> {
    open val item: T? = null
    class White<N:Ord<N>>(begin: N): Ray<N, Nothing>(begin)
    class Shadow<N:Ord<N>, T>(begin: N, override val item: T): Ray<N, T>(begin)
    override fun compareTo(other: Ray<N, *>): Int = begin.compareTo(other.begin)
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is Ray<*, *>) return false
      return begin == other.begin
    }
    override fun hashCode(): Int = Ray::class.hashCode()
  }
}

fun <N:Ord<N>, T> mutableRangeMapOf(vararg item: Pair<RangeIE<N>, T>): MutableRangeMap<N, T>
  = TreeRangeMap<N, T>().also { for ((k, v) in item) it[k] = v }
fun <N:Ord<N>, T> rangeMapOf(vararg item: Pair<RangeIE<N>, T>): RangeMap<N, T> = mutableRangeMapOf(*item)

fun <T> intRangeMapOf(vararg item: Pair<IntRange, T>): RangeMap<Int, T>
  = TreeRangeMap<Int, T>().also { for ((k, v) in item) it[k.first untilIE k.last.inc()] = v }
