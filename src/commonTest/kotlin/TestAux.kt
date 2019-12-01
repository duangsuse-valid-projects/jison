import org.parserkt.ArraySlice
import org.parserkt.Feeder
import org.parserkt.SliceFeeder
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

typealias Operation = () -> Unit

internal inline fun <T> assertTF(self: T, tf: T.() -> Pair<Boolean, Boolean>) {
  val (pos, neg) = self.tf()
  assertTrue(pos); assertFalse(neg)
}

internal inline fun <reified EX: Exception> assertMessageEquals(expected: String, op: Operation) {
  assertEquals(expected, assertFailsWith<EX> { op() }.message)
}

internal fun <T> feederOf(vararg item: T): Feeder<T> = SliceFeeder(ArraySlice(item))
