package org.parserkt.util

/** Object with capacity of saving/restoring its state
 * + [mark]/[reset] call should be paired
 * + [mark] could be called before pairing [reset] */
interface MarkReset
  { fun mark() fun reset() }
/** Run [op] operation in [MarkReset.mark], [op] finally [MarkReset.reset] form. */
fun <R> MarkReset.positional(op: Producer<R>): R = try { mark(); op() } finally { reset() }

/** [MarkReset] with one-variable [saved] state,
 * [stack] is lazily created [mark] */
abstract class StateStackMarkReset<ST>: MarkReset {
  protected abstract var saved: ST
  private val stack: MutableList<ST> by lazy(::mutableListOf)
  override fun mark() { stack.add(saved) }
  override fun reset() { saved = stack.removeAtEnd() }
}

/** [MarkReset] with buffer [layer] (`null` when not marking),
 * [stack] is lazily created [mark] */
abstract class BufferStackMarkReset<BUF>: MarkReset {
  protected var layer: BUF? = null
  protected abstract fun emptyBuffer(): BUF
  private val stack: MutableList<BUF> by lazy(::mutableListOf)
  override fun mark() { layer = emptyBuffer(); stack.add(layer!!) }
  /** Add 'before' logic to consume current [layer] */
  override fun reset() { stack.removeAtEnd(); layer = stack.lastOrNull() }
}
