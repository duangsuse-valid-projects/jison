import org.jison.BangParser
import kotlin.test.Test
import kotlin.test.assertEquals

class BangParserTests {
  enum class Banana { Green, Yellow }
  data class MonkeyBanana(val monkey: String, val banana: Banana)

  /** Monkey-to-banana allocation: `MonkeyName (--banana|--ba) (good|bad)` */
  class SimpleBananaParser(vararg args: String): BangParser<List<MonkeyBanana>>(args) {
    override val parseResult = mutableListOf<MonkeyBanana>()
    private var monkey: String? = null
    override fun onItem(item: String) {
      if (monkey != null) fail("The monkey $monkey is angry without banana!")
      else monkey = item
    }
    override fun onBang(part: List<String>) = when (part[1]) {
      "banana", "ba" -> {
        val mkyBanana = MonkeyBanana(monkey ?: fail("missing monkey"), readBanana())
        parseResult.add(mkyBanana)
        monkey = null //the monkey is gone with banana
      }
      else -> throw ParseError("unknown bang")
    }
    private fun readBanana(): Banana = when (next("banana")) {
      "good" -> Banana.Yellow
      "bad" -> Banana.Green
      else -> fail("unknown banana")
    }
  }

  @Test fun completeness() {
    val res = runParse("XiaHou --banana good MiZhu -banana good HouJin --ba bad")
    assertEquals(Banana.Green, res.find { it.monkey == "HouJin" }!!.banana)
    assertEquals(MonkeyBanana("XiaHou", Banana.Yellow), res.first())
  }
  @Test fun soundness() {
    assertMessageEquals("The monkey XiaHou is angry without banana!") { runParse("XiaHou MiZhu") }
    assertMessageEquals("[-ba, ba]: missing monkey") { runParse("-ba nana") }
    for (bang in setOf("-cool-bang", "--cool!"))
      assertMessageEquals("[$bang, ${bang.trimStart('-')}]: unknown bang") { runParse(bang) }
    assertMessageEquals("[--banana, banana]: unknown banana") { runParse("XiaHou --banana ???") }
  }

  private fun assertMessageEquals(expected: String, op: Operation)
    { assertMessageEquals<BangParser.ParseError>(expected, op) }
  private fun runParse(input: String): List<MonkeyBanana>
    = SimpleBananaParser(*input.split(' ').toTypedArray())()
}