package org.jison.vis

import org.jison.Json
import org.jison.JsonVisitor
import org.jison.translateMap
import org.jison.visitedBy
import org.parserkt.util.Cnt
import org.parserkt.util.Operation

class Dumper(private val indent: String): JsonVisitor<Unit> {
  constructor(n_space: Cnt): this(' ' repeatFor n_space)
  private val buffer = StringBuilder()
  private var currentLevel = 0
  override fun toString(): String = buffer.toString()
  fun clear() { buffer.clear() }

  private fun w(text: String): StringBuilder = buffer.append(text)
  private fun w(symbol: Char): StringBuilder = buffer.append(symbol)
  private fun writeIndent()
    { for (_i in 1..currentLevel) buffer.append(indent); buffer.append('\n') }
  private fun withIncLevel(op: Operation) = currentLevel++.run { op() }.also { currentLevel-- }

  private fun dump(v: Json) { v.visitedBy(this) }

  override fun see(dict: Json.Dict) {
    writeIndent(); w('{')
    withIncLevel {
      for ((k, v) in dict.map) {
        writeIndent(); writeStr(k); w(": "); dump(v); w(',')
      }
    }
    writeIndent(); w('}')
  }

  override fun see(ary: Json.Ary) {
    writeIndent(); w('[')
    withIncLevel {
      for (x in ary.xs){
        writeIndent(); dump(x)
      }
    }
    writeIndent(); w(']')
  }

  override fun see(str: Json.Str) {
    writeStr(str.literal)
  }

  private fun writeStr(str: String) {
    w('"')
    w(str.translate(mapping = backTranslate))
    w('"')
  }

  override fun see(num: Json.Num) {
    w(num.i.toString())
  }

  override fun see(boo: Json.Bool) {
    w(boo.toString())
  }

  override fun see(nil: Json.Nil) {
    w("null")
  }
}
internal infix fun Char.repeatFor(n: Cnt): String {
  val ary = Array<Char>(n) {this}
  return ary.joinToString("")
}
val backTranslate = translateMap.entries.map { it.value to it.key }.toMap()
private fun String.translate(escape: Char = '\\', mapping: Map<Char, Char>): String {
  val src = toList(); val sb = StringBuilder()
  var i=0; while (i <= src.indices.last) {
    val c = src[i]; i++
    if (c == escape) { sb.append(src[i].let(mapping::get)) }
    else { sb.append(c) }
  }
  return sb.toString()
}
