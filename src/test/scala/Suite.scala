import scala.concurrent.duration.Duration
import scala.collection.mutable.ArrayBuffer
import javax.lang.model.`type`.UnknownTypeException
import scala.collection.mutable.ListBuffer
import scala.annotation.switch

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
    assertEquals(on_base, "on base")
  }

  f.test("fixture setup context is set") { _ =>
    assertEquals(on_base, "on base")
    assertEquals(on_setup, "on setup") 
  }

  f.test(".fail test fails".fail) { _ =>
    assert(false)
  }
}

class ControlStructures extends BaseFixture {

  f.test("control structures are expressions") { _ =>
    val is = if 1 > 0 then "greater" else "less"
    assertEquals(is, "greater")
  }

  f.test("scala 3 ifs don't need braces") { _ => 

    val x = 
      if 0 == 1 then
        val a = 2
        a + 3
      else if 2 == 2 then
        val b = 3
        b - 1
      else
        val c = 4
        c / 2
      end if // optional

    assertEquals(x, 2)
  }

  f.test("for loop generator") { _ => 
    var ints2 = ArrayBuffer.empty[Int]
    var ints3 = ArrayBuffer.empty[Int]

    // one-liner
    for i <- ints do ints2 += i
    assertEquals(ints2, ArrayBuffer(1, 2, 3, 4, 5))

    // multi-line
    for i <- ints 
    do 
      val j = i * -1
      ints3 += j

    assertEquals(ints3, ArrayBuffer(-1, -2, -3, -4, -5))
  }

  f.test("for loops can have multiple generators") { _ =>
  
    var list = ListBuffer.empty[Char]

    for
      i <- 'a' to 'b'
      j <- 'c' to 'd'
      k <- 'e' to 'f'
    do
      list += i
      list += j
      list += k

    assertEquals(list, ListBuffer(
      'a', 'c', 'e', 
      'a', 'c', 'f', 
      'a', 'd', 'e', 
      'a', 'd', 'f', 
      'b', 'c', 'e', 
      'b', 'c', 'f', 
      'b', 'd', 'e', 
      'b', 'd', 'f',
    ))
  }

  f.test("for loops can iterate maps") { _ =>

    var list = ListBuffer.empty[String]

    val states = Map(
      "AP" -> "Amapá",
      "MT" -> "Mato Grosso",
      "CE" -> "Ceará",
    )

    for (abbrev, full_name) <- states do list += s"$abbrev: $full_name"

    assertEquals(list, ListBuffer(
      "AP: Amapá",
      "MT: Mato Grosso",
      "CE: Ceará",
    ))
  }

  f.test("for loop guards") { _ =>
    var greater = ArrayBuffer.empty[Int]
    for i <- ints
      if i > 3
    do
      greater += i
    assertEquals(greater, ArrayBuffer(4, 5))
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

    assertEquals(last_i, 2)
    assertEquals(last_j, 'b')
  }

  f.test("for yield returns the same data structure with results") { _ =>
    val doubles = for i <- ints yield i * 2
    assertEquals(doubles, ArrayBuffer(2, 4, 6, 8, 10))
  }

  f.test("yield expressions can have a block body") { _ => 
    val names = List("_olivia", "_walter", "_peter")

    val cap_names = for name <- names yield
      val name_without_underscore = name.drop(1)
      name_without_underscore.capitalize

    assertEquals(cap_names, List("Olivia", "Walter", "Peter"))
  }

  f.test("for yield is equivalent to a map expression") { _ =>
    val map_list = (10 to 22).map(_ * 2)
    val for_list = for i <- 10 to 22 yield i * 2
    assertEquals(map_list, for_list)
  }

  f.test("for can yield multiple values") { _ => 

    val results = ArrayBuffer.empty[Int]

    val result = for
      int <- ints if int % 2 == 0
    yield
      results += int
      val square = int * int
      results += square
      results += square * 2

    assertEquals(results, ArrayBuffer(2, 4, 8, 4, 16, 32))
  }

  f.test("while loop syntax") { _ =>
    var i = 0

    while i < 9 do
      i -= 1
      i += 2

    assertEquals(i, 9)
  }

  f.test("while loop with block return") { _ =>
    val result = {
      var x = 0
      while x < 13 do x += 3 
      x
    }
    assertEquals(result, 15)
  }

  f.test("basic match expression") { _ => 
    @switch val i = 3
    var number = ""

    i match
      case 1 => number = "one"
      case 2 => number = "two"
      case 3 => number = "three"
      case  _ => number = "other"

      assertEquals(number, "three")
  }

  f.test("match structures are expressions") { _ => 

   @switch val i = 2

    val number = i match
      case 1 => "one"
      case 2 => "two"
      case 3 => "three"
      case  _ => "other"

      assertEquals(number, "two")
      
  }

  f.test("match expressions can match types") { _ =>
    def matchType(x: Matchable): String = x match
      case s: String => s"String containing: $s"
      case i: Int => "Int"
      case d: Double => "Double"
      case l: List[?] => "List"
      case _ => "Unexpected type"

    assertEquals(matchType("a string"), "String containing: a string")
    assertEquals(matchType(4.92), "Double")
    assertEquals(matchType(List(3, 2, 1)), "List")
  }

  f.test("a lowercase variable on match left captures a default") { _ =>
    val i = 3
    val r = i match
      case 0 => '0'
      case 1 => '1'
      case 2 => '2'
      case x => x

    assertEquals(r, 3)
  }

  f.test("an uppercase variable on match left uses scope") { _ =>
    val i, Y = 3
    val r = i match
      case 0 => '0'
      case 1 => '1'
      case 2 => '2'
      case Y => '3'
      case x => 'x' // unreachable

    assertEquals(r, '3')
  }

  f.test("match can handle multiple values in a single line") { _ =>
      def even_or_odd(n: Int): String =
        n match
          case 1 | 3 | 5 | 7 | 9 => "odd"
          case 2 | 4 | 6 | 8 | 10 => "even"
          case n => s"$n is out of bounds"

      assertEquals(even_or_odd(4), "even")
      assertEquals(even_or_odd(7), "odd")
      assertEquals(even_or_odd(31), "31 is out of bounds")
  }

  f.test("match cases can have guards") { _ =>
    
    def assert(f: Int => String) =
      val pairs = Map(
        1 -> "one",
        5 -> "between 2 and 5",
        10 -> "10 or greater",
        31 -> "10 or greater",
        -8 -> "0 or less",
      )

      for (number, range) <- pairs do assertEquals(f(number), range)

    def get_range(i: Int) =
      i match
        case 1 => "one"
        case a if a > 1 && a < 6 => "between 2 and 5"
        case b if b > 5 && b < 10 => "between 6 and 9"
        case c if c >= 10 => "10 or greater"
        case _ => "0 or less"

    assert(get_range)

    // same example using a more idiomatic, readable syntax:
    def get_range_2(i: Int) =
      i match
        case 1 => "one"
        case a if 1 to 5 contains a => "between 2 and 5"
        case b if 6 to 9 contains b => "between 6 and 9"
        case c if c >= 10 => "10 or greater"
        case _ => "0 or less"

    assert(get_range_2)
  }

    f.test("fields from classes can be extracted") { _ =>

      case class Person(name: String)

      def speak(p: Person) = p match
        case Person(name) if name == "Fred" => s"$name says, Yubba dubba doo"
        case Person(name) if name == "Bam Bam" => s"$name says, Bam bam!"

      assertEquals(speak(Person("Fred")), "Fred says, Yubba dubba doo")
  }

  f.test("extractor methods can initialize values") { _ =>
    import scala.util.Random

    object CustomerID:
      def apply(name: String) = s"$name--${Random.nextLong().abs}"
      def unapply(customerID: String): Option[String] =
        val stringArray: Array[String] = customerID.split("--").nn.map(_.nn)
        if stringArray.tail.nonEmpty then Option(stringArray.head) else None

    val customer = CustomerID("Sukyoung")

    // these three assignments are all equivalent:
    val name1 = customer match
      case CustomerID(name) => name
      case _ => "Couldn't extract name"

    // @unchecked quiets a "pattern binding uses refutable extractor" warning
    val CustomerID(name2) = customer: @unchecked

    @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
    val name3 = CustomerID.unapply(customer).get

    assertEquals(name1, "Sukyoung")
    assertEquals(name1, name2)
    assertEquals(name1, name3)

    // the corresponding instance doesn't really have to exist
    // it will regardless return the unapply result for the given string:
    val CustomerID(name4) = s"Jane--0": @unchecked

    assertEquals(name4, "Jane")

    // dows throw since the body of unapply fails to run for lack of a `-` char
    intercept[scala.MatchError]{
      val CustomerID(name5) = s"Peter-000000000000": @unchecked
      println(name5)
    }
  }

  f.test("try structures are expressions") { _ =>

    var always: String = ""

    val exception = try 
      2/0
    catch
      case e: NumberFormatException => "Got a NumberFormatException"
      case e: ArithmeticException => "Got an ArithmeticException"
    finally 
      always = "This always executes"

    assertEquals(exception, "Got an ArithmeticException")
    assertEquals(always, "This always executes")
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

  f.test("parameters not assigned as val or var are not accessible") { _ =>
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
