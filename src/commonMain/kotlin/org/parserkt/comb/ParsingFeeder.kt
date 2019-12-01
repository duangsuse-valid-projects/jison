package org.parserkt.comb

import org.parserkt.*
import org.parserkt.util.Cnt

/** <[file]>:[line]:[column] #[position] */
data class SourceLocation(var file: String?, var line: Cnt = 1, var column: Cnt = 1, var position: Cnt = 0) {
  init { file = file ?: "<anon>" }
  val tag: String get() = "$file:$line:$column"
  override fun toString(): String = "$tag #$position"
}

/**
 * [Feeder] with source location decoration [quake]
 * + [ScannerOpts] is not supported by this utility, since [ScannerOpts.dropWhileIn] is uncounted
 */
open class ParsingFeeder<out T>(protected open val inner: Feeder<T>,
    private val srcLoc: SourceLocation): Feeder<T> by inner {
  constructor(file: String? = null, slice: Slice<T>): this(SliceFeeder(slice), SourceLocation(file))
  constructor(file: String? = null, stream: Iterator<T>): this(StreamFeeder(stream), SourceLocation(file))
  override fun consume(): T {
    val took = inner.consume()
    updateSrcLoc(took)
    return took
  }
  protected fun updateSrcLoc(item: Any?) {
    ++srcLoc.position; ++srcLoc.column
    when (item) {
      '\n' -> { ++srcLoc.line; srcLoc.column = 1 }
      '\r' -> {
        ++srcLoc.line; srcLoc.column = 1
        if (inner.peek == '\n') inner.consume()
      }
    }
  }
  override fun quake(error: Exception): Nothing = throw ParserError("@$srcLoc: ${error.message}")
}

class BulkParsingFeeder<out T>(override val inner: BulkFeeder<T>, srcLoc: SourceLocation):
  ParsingFeeder<T>(inner, srcLoc), BulkFeeder<T> {
  override fun take(n: Cnt): BulkFeeder.Viewport<T> {
    val took = inner.take(n)
    took.stream().forEach(::updateSrcLoc)
    return took
  }
}

fun parsingFeeder(file: String? = null, str: CharSequence): ParsingFeeder<Char> = ParsingFeeder(file, CharSlice(str))
