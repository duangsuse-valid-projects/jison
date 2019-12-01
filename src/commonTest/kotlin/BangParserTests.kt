import org.jison.BangParser
import kotlin.test.Test
import kotlin.test.assertEquals

class BangParserTests {
  enum class Banana { Green, Yellow }
  data class MonkeyBanana(val monkey: String, val banana: Banana)
  class SimpleBananaParser(vararg args: String): BangParser<List<MonkeyBanana>>(args) {
    private var monkey: String? = null
    private val monkeyBananas = mutableListOf<MonkeyBanana>()
    override fun onItem(item: String) {
      if (monkey != null) throw ParseError("The monkey $monkey is angry without banana!")
      monkey = item
    }
    override fun onBang(part: List<String>) = when (part[1]) {
      "banana", "ba" -> {
        val mkyBanana = MonkeyBanana(monkey ?: fail("missing monkey"), readBanana())
        monkeyBananas.add(mkyBanana)
        monkey = null //the monkey is gone with banana
      }
      else -> throw ParseError("unknown bang")
    }
    private fun readBanana(): Banana = when (next("banana")) {
      "good" -> Banana.Yellow
      "bad" -> Banana.Green
      else -> fail("unknown banana")
    }
    override fun parseResult(): List<MonkeyBanana> = monkeyBananas
  }

  @Test fun completeness() {
    val res = runParse("xiaHou --banana good miZhu -banana good houJin --ba bad")
    assertEquals(Banana.Green, res.find { it.monkey == "houJin" }!!.banana)
    assertEquals(MonkeyBanana("xiaHou", Banana.Yellow), res.first())
  }
  @Test fun soundness() {
    assertMessageEquals("The monkey xiaHou is angry without banana!") { runParse("xiaHou miZhu") }
    assertMessageEquals("[-ba, ba]: missing monkey") { runParse("-ba na") }
    for (bang in setOf("-cool-bang", "--cool!"))
      assertMessageEquals("[$bang, ${bang.trimStart('-')}]: unknown bang") { runParse(bang) }
    assertMessageEquals("[--banana, banana]: unknown banana") { runParse("xiaHou --banana ???") }
  }

  private fun assertMessageEquals(expected: String, op: Operation) = assertMessageEquals<BangParser.ParseError>(expected, op)
  private fun runParse(input: String) = SimpleBananaParser(*input.split(' ').toTypedArray())()
}