package org.parserkt.comb

import org.parserkt.*
import org.parserkt.util.Cnt

/** <[file]>:[line]:[column] #[position] */
data class SourceLocation(var file: String?, var line: Cnt = 1, var column: Cnt = 1, var position: Cnt = 0) {
  init { file = file ?: "<anon>" }
  val tag: String get() = "$file:$line:$column"
  override fun toString(): String = "$tag #$position"
}

val DEFAULT_VIEWPORT = Triple(-10, 10, "")
/** [ParserError.briefView] take size (actual size may lesser): _(l, r, join)_ */
var briefViewport = { _: SliceFeeder<*>? -> DEFAULT_VIEWPORT }

/**
 * [Feeder] with source location decoration [quake]
 * + [ScannerOpts] is not supported by this utility, since [ScannerOpts.dropWhileIn] is uncounted
 */
open class ParsingFeeder<out T>(protected open val inner: Feeder<T>,
    private val srcLoc: SourceLocation): Feeder<T> by inner {
  constructor(slice: Slice<T>, file: String? = null): this(SliceFeeder(slice), SourceLocation(file))
  constructor(stream: Iterator<T>, file: String? = null): this(StreamFeeder(stream), SourceLocation(file))

  override fun consume(): T {
    val took = inner.consume()
    updateSrcLoc(took)
    return took
  }
  protected open fun updateSrcLoc(item: Any?) {
    ++srcLoc.position; ++srcLoc.column
    when (item) {
      '\n' -> { ++srcLoc.line; srcLoc.column = 1 }
      '\r' -> {
        ++srcLoc.line; srcLoc.column = 1
        if (inner.peek == '\n') inner.consume()
      }
    }
  }
  override fun quake(error: Exception): Nothing = throw ParserError("@$srcLoc: ${error.message}", briefView())
  protected open fun briefView(): String? {
    return when (inner) {
      is SliceFeeder<T> -> {
        val cfg = briefViewport(inner as SliceFeeder<T>)
        return (inner as SliceFeeder<T>)[Pair(cfg.first, cfg.second)]
          .stream().toList().joinToString(cfg.third)
      }
      is StreamFeeder<T>, is StreamFeederLookahead1<T> -> {
        val cfg = briefViewport(null)
        return inner.takeItemN(cfg.second).joinToString(cfg.third)
      }
      is BulkFeeder<T> -> {
        val cfg = briefViewport(null)
        return (inner as BulkFeeder<T>).take(cfg.second)
          .stream().toList().joinToString(cfg.third)
      }
      else -> null
    }
  }
}

class BulkParsingFeeder<out T>(override val inner: BulkFeeder<T>, srcLoc: SourceLocation):
  ParsingFeeder<T>(inner, srcLoc), BulkFeeder<T> {
  constructor(inner: BulkFeeder<T>, file: String?): this(inner, SourceLocation(file))

  override fun take(n: Cnt): BulkFeeder.Viewport<T>
    = inner.take(n).also { it.stream().forEach(::updateSrcLoc) }
}

fun parsingFeeder(str: CharSequence, file: String? = null): ParsingFeeder<Char>
  = ParsingFeeder(CharSlice(str), file)

fun bulkParsingFeeder(str: CharSequence, file: String? = null): BulkParsingFeeder<Char>
  = BulkParsingFeeder(SliceFeeder(CharSlice(str)), file)
