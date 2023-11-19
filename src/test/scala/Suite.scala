import scala.concurrent.duration.Duration
import scala.collection.mutable.ArrayBuffer
import javax.lang.model.`type`.UnknownTypeException

abstract class BaseSuite extends munit.FunSuite {
  override val munitTimeout = Duration(10, "sec")
  val on_base: String = "on base"
}

class BaseFixture extends BaseSuite {

  var on_setup: String = ""
  var ints = ArrayBuffer.empty[Int]

  val f = FunFixture[String](
    setup = { _ => {
        ints = ArrayBuffer(1, 2, 3, 4, 5)
        on_setup = "on setup"
        on_setup 
      }
    },
      teardown = { _ => on_setup = "this fixture was torn down" },
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
}

class ControlStructures extends BaseFixture {

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

class DomainModelling extends BaseFixture {

  // Object-oriented

  f.test("traits are similar to interfaces") { _ =>
    trait Speaker:
      def speak(): String

    trait TailWagger:
      def startTail(): Unit = {}
      def stopTail(): Unit = {}

    trait Runner:
      def startRunning(): Unit = {}
      def stopRunning(): Unit = {}

    class Dog(val name: String) extends Speaker, TailWagger, Runner:
      def speak(): String = "Woof!"

    class Cat(val name: String) extends Speaker, TailWagger, Runner:
      def speak(): String = "Meow!"
      override def startRunning(): Unit = {}
      override def stopRunning(): Unit = {}

    val d = Dog("Rover")
    assertEquals(d.name, "Rover")
    val c = Cat("Morris")
    assertEquals(d.speak(), "Woof!")
    assertNotEquals(d.speak(), "Meow!")
  }

  f.test("class declarations create constructors") { _ =>
    class Person(var firstName: String, var lastName: String):
      def getFullName() = s"$firstName $lastName"

    val p = Person("John", "Stephens")
    assertEquals(p.firstName, "John")
    p.lastName = "Legend"
    assertEquals(p.getFullName(), "John Legend")
  }

  f.test("parameters not assigned as val or var exist but are not accessible") { _ =>
    class Person(firstName: String, val lastName: String):
      def getFullName() = s"$firstName $lastName"

    val p = Person("Jane", "Doe")
    // p.firstName // won't compile: Reference Error
    assertEquals(p.getFullName(), "Jane Doe") 
  }

  // Functional
  
  f.test("algebraic sum types can be modeled with enums") { _ =>
    enum CrustSize:
      case Small, Medium, Large

    enum CrustType:
      case Thin, Thick, Regular

    enum Topping:
      case Cheese, Pepperoni, BlackOlives, GreenOlives, Onions

    import CrustSize.*
    val currentCrustSize = Small

    val size = currentCrustSize match
      case Small => "Small crust size"
      case Medium => "Medium crust size"
      case Large => "Large crust size"

    assertEquals(size, "Small crust size")
  }

  f.test("sum types can be defined using enum cases with parameters") { _ =>
    enum Nat:
      case Zero
      case Succ(pred: Nat)

    val number: Nat = Nat.Succ(Nat.Succ(Nat.Zero)) // represents 2

    def increment(n: Nat): Nat = n match
      case Nat.Zero => Nat.Succ(Nat.Zero)
      case Nat.Succ(pred) => Nat.Succ(increment(pred))

    // Nat.Succ(Nat.Succ(Nat.Succ(Nat.Zero))), representing 3
    val incrementedNum: Nat = increment(number) 

    assertEquals(
      incrementedNum,
      Nat.Succ(Nat.Succ(Nat.Succ(Nat.Zero))),
    )
  }

    f.test("product types can be defined using case classes") { _ =>
      case class Musician(
        name: String,
        vocation: String,
      )

      val m = Musician("Reginald", "Singer")
      assertEquals(m.name, "Reginald")
      assertEquals(m.vocation, "Singer")

      // m.name = "Joe" // won't compile: Reassignment to val
      val m2 = m.copy(name = "나윤선")
      assertEquals(m2.name, "나윤선")
    }

    f.test("methods can be chained for less assignments and brevity") { _ =>
      val nums = (1 to 10).toList

      val result = nums.filter(_ > 3)
        .filter(_ < 7)
        .map(_ * 10)

        assertEquals(result, List(40, 50, 60))
    }

    
  }
