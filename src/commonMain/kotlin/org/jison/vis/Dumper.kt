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
  private fun writeIndent() {
    if(currentLevel != 0)
    for (_i in 1..currentLevel) buffer.append(indent) }
  private fun withIncLevel(op: Operation) = currentLevel++.run { op() }.also { currentLevel-- }
  private fun writeNL() = w('\n')

  private fun dump(v: Json) { v.visitedBy(this) }

  override fun see(dict: Json.Dict) {
    writeIndent(); w('{'); writeNL()
    withIncLevel {
      for ((i, kv) in dict.map.entries.withIndex()) {
        val (k ,v) = kv; writeIndent()
          writeStr(k); w(": "); dump(v)
        if (i != dict.map.size.dec()) {
          w(','); writeNL() }
      }
    }
    writeIndent(); w('}'); writeNL()
  }

  private val Json.isMixed get() = this is Json.Ary || this is Json.Dict

  override fun see(ary: Json.Ary) {
    writeIndent(); w('['); writeNL()
    withIncLevel {
      for (x in ary.xs){
        writeIndent(); dump(x)
      }
    }
    writeIndent(); w(']'); writeNL()
  }

  override fun see(str: Json.Str) { writeStr(str.literal) }

  private fun writeStr(str: String) {
    w('"')
    w(str.translate(mapping = backTranslate))
    w('"')
  }

  override fun see(num: Json.Num) { w(num.i.toString()) }
  override fun see(boo: Json.Bool) { w(boo.p.toString()) }
  override fun see(nil: Json.Nil) { w("null") }
}

internal infix fun Char.repeatFor(n: Cnt): String {
  val ary = Array(n) {this}
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
