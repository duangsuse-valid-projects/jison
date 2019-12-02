import org.parserkt.Feeder
import org.parserkt.dropWhile
import org.parserkt.takeItemN
import org.parserkt.takeWhile
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FeederOpsTest {
  private lateinit var feed: Feeder<Int>
  @BeforeTest fun setupFeed() { feed = feederOf(1,2,3,5,9,1) }

  @Test fun dropWhile() {
    feed.dropWhile { it <= 3 }
    assertEquals(listOf(5,9,1), feed.takeItemN(3).toList())
  }

  @Test fun takeWhile() {
    val lt3 = feed.takeWhile { it < 3 }
    assertEquals(listOf(1,2), lt3.toList())
    val gt1 = feed.takeWhile { it > 1 }
    assertEquals(listOf(3,5,9), gt1.toList())
    assertEquals(emptyList(), feed.takeWhile { it > 1 }.toList())
  }

  @Test fun takeItemN() {
    feed.consume()
    assertEquals(listOf(2,3), feed.takeItemN(2).toList())
    assertEquals(listOf(5,9), feed.takeItemN(2).toList())
  }
}