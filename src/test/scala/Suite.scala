import scala.concurrent.duration.Duration

abstract class BaseSuite extends munit.FunSuite {
  override val munitTimeout = Duration(10, "sec")
  val base: Boolean = true
}

class FunFixture extends BaseSuite {

  var check: Boolean = _

  val fixture = FunFixture[Boolean](
    setup = { _ => check = true; base },
    teardown = { (c: Boolean) => var c = false },
  )

  test("base suite is extended") {
    assert(base)
  }

  test(".fail test fails".fail) {
    assertEquals(1, 0)
  }

  fixture.test("fixture check is set during setup") { (check: Boolean) =>
    assert(check)
  }
}

