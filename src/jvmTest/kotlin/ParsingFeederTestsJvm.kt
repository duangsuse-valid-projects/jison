import org.parserkt.takeWhile
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class ParsingFeederTestsJvm {
  @Test fun inputStreamCharIterator() {
    val s = parsingFeeder("niHao.txt", ByteArrayInputStream("hello 世界".toByteArray()))
    assertEquals('h', s.consume())
    assertEquals('e', s.consume())
    assertEquals("llo 世", s.takeWhile { it != '界' }.joinToString(""))
  }
  @Test fun highSurrogateCodePoint() {
    val s = parsingFeeder("emotions.txt", ByteArrayInputStream("\uD83D\uDE06 \uD83D\uDE07".toByteArray()))
    assertEquals("😆", listOf(s.consume(), s.consume()).joinToString(""))
    assertEquals(' ', s.consume())
    assertEquals("😇", listOf(s.consume(), s.consume()).joinToString(""))
  }
}