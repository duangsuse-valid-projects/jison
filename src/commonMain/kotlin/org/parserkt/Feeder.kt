package org.parserkt

import org.parserkt.util.Cnt
import org.parserkt.util.Predicate
import org.parserkt.util.MarkReset
import org.parserkt.util.positional

/** Parser-friendly [peek]/[consume] modeled [Iterator], [quake] can give detailed source location */
interface Feeder<out T> {
  val peek: T
  fun consume(): T
  fun quake(error: Exception): Nothing = throw error
}
/** [Feeder] with bulk [take] */
interface BulkFeeder<out T>: Feeder<T> {
  fun take(n: Cnt): Viewport<T>
  /** After getting [Viewport] with [take],
   * + [consume] to actual move data pointer
   * + [ignore] to simply ignore the viewport got
   * + Other operation that _mutates data stream_ is __INACCESSIBLE__ before making decision above */
  interface Viewport<out T>: Slice<T>
    { fun consume(): Slice<T>
      fun ignore() }
}

interface ScannerOpts<T> {
  fun takeUntilIn(terminator: Set<T>): Slice<T>
  fun dropWhileIn(ignore: Set<T>)
}

const val PROTECTED_STREAM = "Cannot mutate stream while take staging"
class SliceFeeder<T>(private val stream: SliceStream<T>): BulkFeeder<T>, ScannerOpts<T> {
  constructor(input: Slice<T>): this(input.stream())
  override val peek: T get() = stream.peek
  override fun consume(): T {
    check(canMutatePtr) { PROTECTED_STREAM }
    return stream.next()
  }

  private var canMutatePtr = true
  override fun take(n: Cnt): BulkFeeder.Viewport<T> = stream.positional {
    check(canMutatePtr) { PROTECTED_STREAM }
    val slice = stream.take(n)
    canMutatePtr = false
    object : BulkFeeder.Viewport<T>, Slice<T> by slice {
      private var willConsume = true
      override fun consume(): Slice<T> { if(willConsume)stream.take(n); canMutatePtr = true; return slice }
      override fun ignore() { willConsume = false; canMutatePtr = true }
    }
  }
  override fun dropWhileIn(ignore: Set<T>): Unit = dropWhile { it in ignore }
  override fun takeUntilIn(terminator: Set<T>): Slice<T> = takeWhile { it !in terminator }.toList().let(::ListSlice)
  operator fun get(viewport: Viewport): Slice<T> = stream[viewport]
}

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
fun <T> StreamFeeder<T>.take(n: Cnt): Sequence<T> {
  var count = 0
  return takeWhile { count++ != n }
}

/** [Feeder] with no-[MarkReset] support (Left Lookahead-1) */
class StreamFeederLookahead1<T>(private val stream: PeekStream<T>): Feeder<T> {
  constructor(input: Iterator<T>): this(SavePeekStream(input))
  override val peek: T get() = stream.peek
  override fun consume(): T = stream.next()
}

class StreamFeeder<T>(private val stream: MarkResetPeekStream<T>): Feeder<T>, MarkReset by stream {
  constructor(input: Iterator<T>): this(MarkResetPeekStream(input))
  override val peek: T get() = stream.peek
  override fun consume(): T = stream.next()
}
