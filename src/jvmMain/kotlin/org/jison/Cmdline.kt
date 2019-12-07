package org.jison

import org.parserkt.util.Cnt
import org.parserkt.util.justAdd
import java.io.File

/**
 * jison (indent/dump) | (toyaml) | (path)
 * + -indent size=2
 * + (file...) -dump
 * + (file) -toyaml (file)
 * + (file) -path (path) -path (path)...
 */
class Cmdline(vararg argv: String): BangParser<List<Cmdline.Command>>(argv) {
  sealed class Command {
    data class Dump(val indent: Cnt, val files: List<File>): Command()
    data class ToYaml(val src: File, val dst: File): Command()
    data class JsonPath(val file: File, val paths: List<String>): Command()
  }
  private val cmd: MutableList<Command> = mutableListOf()
  private val files: MutableList<File> = mutableListOf()
  private val paths: MutableList<String> = mutableListOf()
  private var indent: Cnt? = null
  override fun onItem(item: String) {
    if (File(item).canRead()) { files.justAdd(File(item)) }
      else { fail("Cannot open input $item") }
    if (paths.isNotEmpty()) {
      cmd.add(Command.JsonPath(nextF("json"), paths.toList()))
      paths.clear()
    }
  }
  override fun onBang(part: List<String>): Unit = when (part[1]) {
    "dump" -> { cmd.add(Command.Dump(indent ?: 2, files.toList())); files.clear() }
    "indent" -> { check(indent == null); indent = next("size").toInt() }
    "to"+"yaml" -> { cmd.add(Command.ToYaml(nextF("source json"), nextNewF("dest yaml"))); Unit }
    "path" -> { paths.add(next("one path")); Unit }
    else -> fail("unknown ${part[1]}")
  }
  override val parseResult get() = cmd

  private fun nextF(name: String): File = next(name).let(::File).also { check(it.canRead()) }
  private fun nextNewF(name: String): File = next(name).let(::File).also { check(it.mkdirs() && it.createNewFile()) }
}