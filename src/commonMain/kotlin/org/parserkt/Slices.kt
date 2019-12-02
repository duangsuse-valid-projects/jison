package org.parserkt
import org.parserkt.util.*

interface Sized { val size: Cnt }
val Sized.isEmpty: Boolean get() = size == 0
val Sized.isNotEmpty: Boolean get() = size != 0
val Sized.lastIndex: Idx get() = size.dec()
val Sized.indices: IdxRange get() = 0..lastIndex
fun Sized.coerceInbound(index: Idx) = index.coerceIn(indices)
fun Sized.coerceInbound(indices: IdxRange) = coerceInbound(indices.first)..coerceInbound(indices.last)

/** Abstract sequence of [T] with [size]/[get]/[slice] feature */
interface Slice<out T>: Sized, FiniteStreamed<T> {
  operator fun get(index: Idx): T
  operator fun get(indices: IdxRange): Slice<T>
  override fun stream(): SliceStream<T> = SliceStream(this)
}

class ArraySlice<out T>(private val ary: Array<T>): Slice<T> {
  override val size = ary.size
  override fun get(index: Idx): T = ary[index]
  override fun get(indices: IdxRange): Slice<T> = ArraySlice(ary.sliceArray(indices))
}

class ListSlice<out T>(private val list: List<T>): Slice<T> {
  override val size: Cnt get() = list.size
  override fun get(index: Idx): T = list[index]
  override fun get(indices: IdxRange): Slice<T> = ListSlice(list.subList(indices))
}

class CharSlice(private val charSeq: CharSequence): Slice<Char> {
  override val size: Cnt get() = charSeq.length
  override fun get(index: Idx): Char = charSeq[index]
  override fun get(indices: IdxRange): Slice<Char> = CharSlice(charSeq.subSequence(indices))
}

typealias Viewport = Pair<Int, Int>
class SliceStream<out T>(private val slice: Slice<T>): PeekStream<T>, TakeStream<T>, StateStackMarkReset<Idx>() {
  private var ptr: Idx = 0
  override var saved: Idx
    get() = ptr
    set(oldPtr) { ptr = oldPtr }
  override val isEnd: Boolean get() = ptr == slice.lastIndex.inc()
  override val peek: T get() = try { slice[ptr] }
    catch (e: IndexOutOfBoundsException) { throw FiniteStream.StreamEnd(e.message) }
  override fun next(): T = peek.also { ++ptr }
  override fun take(n: Cnt): Slice<T> {
    val newExclusive = ptr+n.dec()
    if (newExclusive > slice.lastIndex) throw FiniteStream.StreamEnd("take $n at $ptr/${slice.size}")
    return slice[ptr..newExclusive].also { ptr += n }
  }
  operator fun get(viewport: Viewport): Slice<T> = slice[slice.coerceInbound(ptr+viewport.first..ptr+viewport.second)]
}
