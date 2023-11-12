@main def hello: Unit =
  val a_char = ','
  val immutable = "Scala"
  var mutable = "lo"
  println(s"Hel${mutable}es$a_char $immutable!")

// ??? // throws unimplemented

// Suppresses a WartRemover warning
// @SuppressWarnings(Array("org.wartremover.warts.Var"))

// imperative
import scala.collection.mutable.ListBuffer

def double(ints: List[Int]): List[Int] =
  val buffer = new ListBuffer[Int]()
  for i <- ints do
    buffer += i * 2
  buffer.toList

val old_numbers = List(1, 2, 3)
val numbers = double(old_numbers)

// functional
val f_numbers = old_numbers.map(_ * 2)

// case class 
final case class Person(
  name: String,
  surname: String,
)

def p(name: String, surname: String): Person =
  Person(name, surname)

val jane = p("Jane", "Doe")

// pattern matching
def isTruthy(a: Matchable) = a match
  case 0 | "" => false
  case _ => true

def morePatternMatching =
  val i = 3
  val numAsString = i match
  case 1 | 3 | 5 | 7 | 9 => "odd"
  case 2 | 4 | 6 | 8 | 10 => "even"
  case _ => "too big"


// implicit value
implicit val reverseOrdering: Ordering[Int] = Ordering.Int.reverse
// List(1, 2, 3).sorted // List(3, 2, 1)
