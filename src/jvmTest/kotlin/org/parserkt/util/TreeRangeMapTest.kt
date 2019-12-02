package org.parserkt.util

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TreeRangeMapTest {
  private val map = TreeRangeMap<Int, String>()
  @BeforeTest fun setupMap() {
    map[0 untilIE 5] = "doge"
    map[5 untilIE 10] = "cate"
    map[11 untilIE 12] = "1"
    map[20 untilIE 21] = "9"
    map[-9 untilIE 0] = "xd"
  }

  @Test fun get() {
    for (i in 0 until 5) assertMapItemEquals("doge", i)
    for (i in 5 until 10) assertMapItemEquals("cate", i)
    for (i in 11 until 12) assertMapItemEquals("1", i)
    for (i in 12 until 20) assertMapItemEquals(null, i)
    for (i in 20 until 21) assertMapItemEquals("9", i)
    for (i in (-9) until 0) assertMapItemEquals("xd", i)
  }

  @Test fun limitations() {
    map[1 untilIE 2] = "doXge"
    assertMapItemEquals("doge", 0)
    assertMapItemEquals("doXge", 1)
    for (i in 2 until 5) assertMapItemEquals(null, i)

    map[-9 untilIE 0] = "newXD"
    for (i in (-9) until 0) assertMapItemEquals("newXD", i)
    assertMapItemEquals("doge", 0)
  }

  private val cartoons = intRangeMapOf(
    1980..1983 to "舒克和贝塔",
    1989..2000 to "编不出了！",
    2010..2014 to "喜羊羊",
    2015..2019 to "柯南",
    2020..2030 to "熊出没"
  )
  @Test fun quickConstructor() {
    assertEquals("舒克和贝塔", cartoons[1980])
    assertEquals(null, cartoons[1987])
    assertEquals("编不出了！", cartoons[2000])
    assertEquals("熊出没", cartoons[2029])
  }

  private fun assertMapItemEquals(expected: String?, index: Int)
    = assertEquals(expected, map[index], "@$index")
}