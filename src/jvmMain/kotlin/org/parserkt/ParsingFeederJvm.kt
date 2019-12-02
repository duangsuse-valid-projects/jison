package org.parserkt

import org.parserkt.comb.ParsingFeeder
import org.parserkt.comb.SourceLocation
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class InputStreamCharIterator(private val reader: Reader): Iterator<Char> {
  constructor(i_stream: InputStream, charset: Charset): this(InputStreamReader(i_stream, charset))
  override fun hasNext(): Boolean = reader.ready()
  override fun next(): Char = reader.read().toChar()
}

fun parsingFeeder(i_stream: InputStream, cs: Charset = StandardCharsets.UTF_8, file: String? = null): ParsingFeeder<Char>
  = ParsingFeeder(InputStreamCharIterator(i_stream, cs), file)

fun parsingFeederLL1(i_stream: InputStream, cs: Charset = StandardCharsets.UTF_8, file: String? = null): ParsingFeeder<Char>
  = ParsingFeeder(StreamFeederLookahead1(InputStreamCharIterator(i_stream, cs)), SourceLocation(file))
