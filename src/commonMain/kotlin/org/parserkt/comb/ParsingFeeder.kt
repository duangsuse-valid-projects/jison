package org.parserkt.comb

import org.parserkt.*
import org.parserkt.util.Cnt
import org.parserkt.util.MarkReset

/** <[file]>:[line]:[column] #[position] */
data class SourceLocation(var file: String?, var line: Cnt = 1, var column: Cnt = 1, var position: Cnt = 0) {
  init { file = file ?: "<anon>" }
  val tag: String get() = "$file:$line:$column"
  override fun toString(): String = "$tag #$position"
}

/** Default [briefViewport] is __(-10, 10]__ join by `""` */
val DEFAULT_VIEWPORT = Triple(-10, 10.dec(), "")
/** [ParserError.briefView] take offset (actual size may lesser): _(l, r, join)_ (inclusive) */
var briefViewport = { _: Feeder<*>? -> DEFAULT_VIEWPORT }

/**
 * [Feeder] with source location decoration [quake]
 * + [ScannerOpts] is not supported by this utility, since [ScannerOpts.dropWhileIn] is uncounted
 */
open class ParsingFeeder<out T>(protected open val inner: Feeder<T>,
    protected val srcLoc: SourceLocation): Feeder<T> by inner, MarkReset {
  constructor(slice: Slice<T>, file: String? = null): this(SliceFeeder(slice), SourceLocation(file))
  constructor(stream: Iterator<T>, file: String? = null): this(StreamFeeder(stream), SourceLocation(file))

  override fun consume(): T = inner.consume().also(::updateSrcLoc)
  override fun quake(error: Exception): Nothing = throw ParserError("@$srcLoc: ${error.message}", briefView())
  protected open fun updateSrcLoc(item: Any?) {
    ++srcLoc.position; ++srcLoc.column
    when (item) {
      '\n' -> { ++srcLoc.line; srcLoc.column = 1 }
      '\r' -> {
        ++srcLoc.line; srcLoc.column = 1
        if (inner.peek == '\n') inner.consume() }
    }
  }

  protected open fun briefView(): String? {
    val cfg = briefViewport(inner)
    return when (val innerFeed = inner) {
      is SliceFeeder<T> -> innerFeed[Pair(cfg.first, cfg.second)]
        .stream().asSequence().joinToString(cfg.third) //NOTE: data pointer will not mutate
      is StreamFeeder<T>, is StreamFeederLookahead1<T> -> innerFeed
        .takeItemN(cfg.second).joinToString(cfg.third)
      is BulkFeeder<T> -> innerFeed.take(cfg.second).consume()
        .stream().asSequence().joinToString(cfg.third)
      else -> null
    }
  }

  override fun mark() { (inner as? MarkReset)?.mark() }
  override fun reset() { (inner as? MarkReset)?.reset() }
  override fun unmark() { (inner as? MarkReset)?.unmark() }
}

class BulkParsingFeeder<out T>(override val inner: BulkFeeder<T>, srcLoc: SourceLocation):
  ParsingFeeder<T>(inner, srcLoc), BulkFeeder<T> {
  constructor(inner: BulkFeeder<T>, file: String?): this(inner, SourceLocation(file))

  private fun updateSrcLoc(items: Slice<Any?>) {
    srcLoc.position += items.size.dec()
    for (i in items.indices) when (items[i]) {
      '\n' -> { ++srcLoc.line }
      '\r' -> {
        if (i.inc() == items.size
          ||items[i.inc()] != '\n') ++srcLoc.line
      }
    }
    val oldColumn = srcLoc.column
    findMark@ for ((i, c) in items.indices.reversed().withIndex()) when (items[c]) {
      '\r' -> { srcLoc.column = items.size-i; break@findMark }
      '\n' -> { srcLoc.column = items.size-i
        if (items[c.dec()] == '\r') --srcLoc.column
        break@findMark }
    }
    if (srcLoc.column == oldColumn) srcLoc.column += items.size.dec() // in-line
  }

  override fun take(n: Cnt): BulkFeeder.Viewport<T> = inner.take(n).let { view ->
    object: BulkFeeder.Viewport<T> by view {
      override fun consume(): Slice<T> = view.consume().also(this@BulkParsingFeeder::updateSrcLoc)
    } }
}

fun parsingFeeder(str: CharSequence, file: String? = null): ParsingFeeder<Char>
  = ParsingFeeder(CharSlice(str), file)

fun bulkParsingFeeder(str: CharSequence, file: String? = null): BulkParsingFeeder<Char>
  = BulkParsingFeeder(SliceFeeder(CharSlice(str)), file)
