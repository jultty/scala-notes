// see https://scalameta.org/munit/docs/getting-started.html
class Suite extends munit.FunSuite {
  test("test engine baseline") {
    val obtained = 42
    val expected = 42
    assertEquals(obtained, expected)
  }
}
