package org.jison

import org.jison.vis.Dumper
import org.parserkt.comb.BulkParsingFeeder
import org.parserkt.comb.ParserError
import org.parserkt.comb.bulkParsingFeeder
import java.io.File

object Main {
  @JvmStatic fun main(vararg args: String) {
    val ps = Cmdline(*args)
    val commands = ps.invoke()
    println(commands)
    for (cmd in commands) when (cmd) {
      is Cmdline.Command.Dump -> {
        val dumper = Dumper(cmd.indent)
        val inputs = openFiles(cmd.files)
        parses(inputs).forEach { it.visitedBy(dumper) }
        print(dumper.toString())
      }
    }
  }

  private fun parses(inputs: List<BulkParsingFeeder<Char>>): List<Json> = try {
    inputs.mapNotNull(JsonParser.file) } catch (e: ParserError) { println("${e.message}\n```\n${e.briefView}\n```"); emptyList() }

  private fun openFiles(files: List<File>): List<BulkParsingFeeder<Char>>
    = files.map { bulkParsingFeeder(it.readText(), it.canonicalPath) }
}