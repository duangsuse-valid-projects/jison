package org.parserkt
import org.parserkt.util.*

interface FiniteStream<out T> {
  abstract val isEnd: Boolean
  abstract operator fun next(): T
  class StreamEnd(message: String? = null): Exception(message ?: "no more item")
  operator fun hasNext(): Boolean = !isEnd
  operator fun iterator(): FiniteStream<T> = this
}
interface FiniteStreamed<out T>
  { fun stream(): FiniteStream<T> }

/** [FiniteStream] with [peek] capacity,
 * + [peek] return [next] without moving data pointer
 * + [peek] should not be called twice before [next] */
interface PeekStream<out T>: FiniteStream<T>
  { val peek: T }
interface TakeStream<out T>: FiniteStream<T>
  { fun take(n: Cnt): Slice<T> }

/**
 * [PeekStream] with saved lookahead implementation
 * + Pre-[next] in `init`: `a (b : c : d : [])`
 * + [tailConsumed] means, (we pre-took 1 item of [iterator]),
 * so [SavePeekStream]'s [hasNext] will give 1 more `true`, for pre-fetched item
 */
class SavePeekStream<out T>(private val iterator: Iterator<T>): PeekStream<T> {
  private var tailConsumed = false
  private var lastItem: T
  init { lastItem = iterator.next() }

  override val peek: T get() = lastItem
  override val isEnd: Boolean get() = !iterator.hasNext() && tailConsumed
  override fun next(): T = if (iterator.hasNext())
    peek.also { lastItem = iterator.next() }
    else if (!tailConsumed)
      { tailConsumed = true; peek }
    else //tailConsumed
      { throw FiniteStream.StreamEnd() }
}
/** Stream [MarkReset] based on [BufferStackMarkReset], performance for [peek] is not compromised */
class MarkResetPeekStream<T>(private val iterator: Iterator<T>): BufferStackMarkReset<MutableList<T>>(), PeekStream<T> {
  override fun emptyBuffer(): MutableList<T> = mutableListOf()
  private val resetting: MutableList<T> = mutableListOf()

  override val isEnd: Boolean get() = !iterator.hasNext() && resetting.isEmpty()
  override fun next(): T {
    val next = if (resetting.isNotEmpty())
      resetting.removeAtBegin() else
        try { iterator.next() }
        catch (_: NoSuchElementException) { throw FiniteStream.StreamEnd() }
    layer?.add(next)
    return next
  }
  override val peek: T get() = positional { next() }
  override fun reset() { layer?.let { resetting.addAll(it) }; super.reset() }
}
