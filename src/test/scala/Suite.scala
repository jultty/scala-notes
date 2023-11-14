import scala.concurrent.duration.Duration

abstract class BaseSuite extends munit.FunSuite {
  override val munitTimeout = Duration(10, "sec")
  val on_base: String = "on base"
}

class FunFixture extends BaseSuite {

  var on_setup: String = ""

  val fixture = FunFixture[String](
    setup = { _ => on_setup = "on setup"; on_setup },
    teardown = { _ => on_setup = "" },
  )

  test("base suite is extended") {
    assertEquals(clue(on_base), "on base")
  }

  test(".fail test fails".fail) {
    assertEquals(1, 0)
  }

  fixture.test("fixture check is set during setup") { _ =>
    assertEquals(clue(on_setup), "on setup") 
  }
}

