package org.jison

/**
 * Base class for argument parsers
 * + [onItem] for patterns like `apple.kt banana.kt ...`
 * + [onBang], [next] for `--user 1 --where boy 14`
 * + [parseResult] to generate actual result, state-machine or builder is recommended
 * + [invoke] can do actual parse using handlers above, then result [parseResult], or [ParseError] if failed
 * + The default [bangRegex] will match `--bang` / `-bang`
 */
abstract class BangParser<R>(private val part: Array<out String>) {
  protected abstract fun onItem(item: String)
  protected abstract fun onBang(part: List<String>)
  protected abstract val parseResult: R

  class ParseError(message: String): Exception(message)
  protected open val bangRegex = Regex("^--?(.*)$")
  private var position = 0
  private val argument get() = part[position]
  private val isEnd get() = position > part.lastIndex

  protected fun next(name: String): String { ++position
    if (isEnd) fail("missing $name")
    else return argument }
  protected fun fail(message: String): Nothing = throw ParseError(message)

  operator fun invoke(): R {
    while (!isEnd) {
      bangRegex.find(argument)?.groupValues?.let {
        try { onBang(it) }
        catch (e: ParseError) { throw ParseError("$it: ${e.message}") }
      } ?: onItem(argument)
      ++position
    }
    return parseResult
  }
}