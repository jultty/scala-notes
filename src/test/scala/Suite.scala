import scala.concurrent.duration.Duration
import scala.collection.mutable.ArrayBuffer

abstract class BaseSuite extends munit.FunSuite {
  override val munitTimeout = Duration(10, "sec")
  val on_base: String = "on base"
}

class FunFixture extends BaseSuite {

  var on_setup: String = ""
  var ints = ArrayBuffer.empty[Int]

  val f = FunFixture[String](
    setup = { _ => {
        ints = ArrayBuffer(1, 2, 3, 4, 5)
        on_setup = "on setup"
        on_setup 
      }
    },
    teardown = { _ => on_setup = "" },
  )

  f.test("base suite is extended") { _ =>
    assertEquals(clue(on_base), "on base")
  }

  f.test("fixture setup context is set") { _ =>
    assertEquals(clue(on_base), "on base")
    assertEquals(clue(on_setup), "on setup") 
  }

  f.test(".fail test fails".fail) { _ =>
    assert(false)
  }

  f.test("control structures are expressions") { _ =>
    val is = if 1 > 0 then "greater" else "less"
    assertEquals(clue(is), clue("greater"))
  }

  f.test("for loop generator") { _ => 
    var ints2 = ArrayBuffer.empty[Int]
    for i <- ints do ints2 += i
    assertEquals(clue(ints2), clue(ArrayBuffer(1, 2, 3, 4, 5)))
  }

  f.test("for loop guards") { _ =>
    var greater = ArrayBuffer.empty[Int]
    for i <- ints
      if i > 3
    do
      greater += i
    assertEquals(clue(greater), clue(ArrayBuffer(4, 5)))
  }

  f.test("for with multiple guards and generators") { _ =>

    var last_i = -1
    var last_j = 'Z'

    for
      i <- 1 to 3
      j <- 'a' to 'c'
      if i == 2
      if j == 'b'
    do
      last_i = i 
      last_j = j

    assertEquals(clue(last_i), clue(2))
    assertEquals(clue(last_j), clue('b'))
  }

  f.test("for yield returns the same data structure with results") { _ =>
    val doubles = for i <- ints yield i * 2
    assertEquals(doubles, ArrayBuffer(2, 4, 6, 8, 10))
  }

  f.test("for can have multiple yield expressions") { _ => 

    val results = ArrayBuffer.empty[Int]

    val result = for {
      int <- ints if int % 2 == 0
    } yield {
      results += int
      val square = int * int
      results += square
      results += square * 2
    }

    assertEquals(clue(results), clue(ArrayBuffer(2, 4, 8, 4, 16, 32)))
  }

  f.test("basic match expression") { _ => 
    val i = 3
    var number = ""

    i match
      case 1 => number = "one"
      case 2 => number = "two"
      case 3 => number = "three"
      case  _ => number = "other"

      assertEquals(clue(number), "three")
  }

  f.test("match expressions return values") { _ => 
    val i = 2

    val number = i match
      case 1 => "one"
      case 2 => "two"
      case 3 => "three"
      case  _ => "other"

      assertEquals(clue(number), "two")
      
  }

  f.test("match expressions can match types") { _ =>
    def matchType(x: Matchable): String = x match
      case s: String => s"String containing: $s"
      case i: Int => "Int"
      case d: Double => "Double"
      case l: List[?] => "List"
      case _ => "Unexpected type"

    assertEquals(clue(matchType("a string")), clue("String containing: a string"))
    assertEquals(clue(matchType(4.92)), clue("Double"))
    assertEquals(clue(matchType(List(3, 2, 1))), clue("List"))
  }

  f.test("division by zero throws ArithmeticException") { _ =>

    var always: String = ""

    val exception = try 
      2/0
    catch
      case nfe: NumberFormatException => "Got a NumberFormatException"
      case nfe: ArithmeticException => "Got an ArithmeticException"
    finally 
      always = "This always executes"

    assertEquals(clue(exception), clue("Got an ArithmeticException"))
    assertEquals(clue(always), clue("This always executes"))
  }

  f.test("while loop with block return") { _ =>
    val result = {
      var x = 0
      while x < 13 do x += 3 
      x
    }
    assertEquals(clue(result), 15)
  }
}

