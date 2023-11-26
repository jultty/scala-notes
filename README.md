# scala notes

Notes on studying Scala 3, mainly from reading documentation such as:
- The [Scala Book](https://docs.scala-lang.org/scala3/book/introduction.html)
- The [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/)
- The v3.4 [Scala language specification](https://scala-lang.org/files/archive/spec/3.4/08-pattern-matching.html#extractor-patterns)
- The [Munit docs](https://scalameta.org/munit/docs/getting-started.html)
- A few blogs and videos, see the [footnotes](#References) for all references

Aside from this README, the [tests](src/test/scala/Suite.scala) contain many runnable examples of some concepts explored here and many only explored there.

There is also a [Jupyter notebooks](notebooks) directory containing a few more in-depth explorations of some of the content treated here.

## Syntax

### Assignment

```scala
val nums = List(1, 2, 3)
val p = Person("Martin", "Odersky") // not a built-in
```

### Types

All types inherit from the supertype `Any`. This top type has a subtype called `Matchable`, which comprehends all types that can be pattern matched.

The last these high-level, broader types explained in the "A First Look at Types" section of the Scala Book are the direct children of `Matchable`: `AnyVal` and `AnyRef`/`Object`.

From these two come the more specific types:

`AnyVal` represents **non-nullable value types** such as `Double`, `Float`, `Long`, `Int`, `Short`, `Byte`, `Char`, `Unit`, and `Boolean`.

`AnyRef` represents **reference types**, which includes all non-value types and all user-defined types. It corresponds to Java's `java.lang.Object`.[^14] This includes strings, classes, objects, functions and compound types like lists and arrays.

```scala
val list: List[Any] = List(
  "a string",
  732,  // an integer
  'c',  // a character
  '\'', // a character with a backslash escape
  true, // a boolean value
  () => "an anonymous function returning a string"
)

list.foreach(element => println(element))
// a string
// 732
// c
// '
// true
// <function>
```

#### Unit
> `Unit` is a value type which carries no meaningful information. There is exactly one instance of `Unit` which we can refer to as: `()`. […] In statement-based languages, `void` is used for methods that don’t return anything. If you write methods in Scala that have no return value, Unit is used for the same purpose.[^14]

#### Type inference
```scala
val i = 123 // defaults to int
val  x = 1.0 // defaults to double

val s = "Bill" // string
val c = 'a' // char

val x = 1_000L   // val x: Long = 1000
val y = 2.2D     // val y: Double = 2.2
val z = -3.3F    // val z: Float = -3.3

// other bases and notations
val q = .25      // val q: Double = 0.25
val r = 2.5e-1   // val r: Double = 0.25
val s = .0025e2F // val s: Float = 0.25
val a = 0xACE    // val a: Int = 2766
val b = 0xfd_3aL // val b: Long = 64826
```

#### String

For multiline String, indentation can be stripped like this:

```scala
val quote = """The essence of Scala:
               |Fusion of functional and object-oriented
               |programming in a typed setting.""".stripMargin
```

Scala has three builtin string interpolation methods: `s`, `f` and `raw`

```scala
println(s"$name is $age years old")   // "James is 30 years old"
println(s"2 + 2 = ${2 + 2}")   // "2 + 2 = 4"
val x = -1
println(s"x.abs = ${x.abs}")   // "x.abs = 1"
println(s"New offers starting at $$14.99")   // "New offers starting at $14.99"
println(s"""{"name":"James"}""")     // `{"name":"James"}`
println(s"""name: "$name",
           |age: $age""".stripMargin)
```

The `f` interpolator allows for specifying the expected type:

```scala
println(f"$name%s is $height%2.2f meters tall")  // "James is 1.90 meters tall"
```

> The `f` interpolator makes use of the string format utilities available from Java. The formats allowed after the `%` character are outlined in the [Formatter javadoc](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Formatter.html#detail). If there is no `%` character after a variable definition a formatter of `%s` (`String`) is assumed. Finally, as in Java, use `%%` to get a literal `%` character in the output string.[^17]

The `raw` interpolator will not interpret escape codes like `s` will:

```scala
scala> s"a\nb"
res0: String =
a
b
scala> raw"a\nb"
res1: String = a\nb
```

#### Casting

Casting is allowed in this direction:

    Byte -> Short -> Int -> Long -> Float -> Double

Also, `Char` can be cast to `Int`.

```scala
val b: Byte = 127
val i: Int = b  // 127
val x: Long = 987654321
val y: Float = x.toFloat  // 9.8765434E8 (`.toFloat` is required because of precision loss)
val z: Long = y  // Error
```

#### Nothing and null

In the Scala type hierarchy, `Nothing` is a subtype of all types, including `Null`, which itself is a subtype of all `AnyRef` children ([see the book's diagram](https://docs.scala-lang.org/scala3/book/first-look-at-types.html)).

Although it is possible to use `Null` as a subtype of `AnyRef`, there are [compiler option](https://docs.scala-lang.org/scala3/reference/experimental/explicit-nulls.html) that will make `AnyRef` children non-nullable and cause implicit `null` initialization errors.

The following can be added to the project settings in `build.sbt` to achieve this:

```scala
scalacOptions ++= Seq(
  "-Yexplicit-nulls",
  "-Ysafe-init",
),
```

Now for `null` to work a value type must use a union type:[^15]

```scala
val x: String = null // error: found `Null`, but required `String`
val x: String | Null = null // OK
```

This may cause some Java types like strings to [behave more strictly](https://stackoverflow.com/q/70747784). For strings, it may be fixed by the [introduction](https://github.com/lampepfl/dotty/pull/15096) of `UnsafeJavaReturn`. Seems it's preferable/easier to use [`Option`](https://scala-lang.org/api/3.x/scala/Option.html) where possible to handle the possibilities in a more robust way.[^16]

### Methods

```scala
def methodName(param1: Type1, param2: Type2): ReturnType =
  // body
```

```scala
def concatenate(s1: String, s2: String): String = s1 + s2
def sum(a: Int, b: Int) = a + b // return types can be inferred
```

Parameters can have default values:

```scala
def makeConnection(url: String, timeout: Int = 5000): Unit =
  println(s"url=$url, timeout=$timeout")

makeConnection("https://localhost")         // url=http://localhost, timeout=5000
makeConnection("https://localhost", 2500)   // url=http://localhost, timeout=2500
```

Parameters can be set by their names:

```scala
makeConnection(
  url = "https://localhost",
  timeout = 2500
)
```

#### Extensions

> The `extension` keyword declares that you’re about to define one or more extension methods on the parameter that’s put in parentheses. As shown with this example, the parameter `s` of type `String` can then be used in the body of your extension methods.

> This next example shows how to add a `makeInt` method to the `String` class. Here, `makeInt` takes a parameter named `radix`. The code doesn’t account for possible string-to-integer conversion errors, but skipping that detail, the examples show how it works:[^12]

```scala
extension (s: String)
  def makeInt(radix: Int): Int = Integer.parseInt(s, radix)

"1".makeInt(2)      // Int = 1
"10".makeInt(2)     // Int = 2
"100".makeInt(2)    // Int = 4
```

### Lambdas
```scala
// long form
nums.map(i => i * 2)
nums.filter(i => i > 1)

// short form
nums.map(_ * 2)
nums.filter(_ > 1)
```

```scala
val a = List(1, 2, 3).map(i => i * 2)   // List(2,4,6)
val b = List(1, 2, 3).map(_ * 2)        // List(2,4,6)
```

```scala
def double(i: Int): Int = i * 2

val a = List(1, 2, 3).map(i => double(i))   // List(2,4,6)
val b = List(1, 2, 3).map(double)           // List(2,4,6)
```

### Higher-order functions
```scala
val xs = List(1, 2, 3, 4, 5)

xs.map(_ + 1)       // List(2, 3, 4, 5, 6)
xs.filter(_ < 3)    // List(1, 2)
xs.find(_ > 3)      // Some(4)
xs.takeWhile(_ < 3) // List(1, 2)
```

> In those examples, the values in the list can’t be modified. The List class is immutable, so all of those methods return new values, as shown by the data in each comment.[^3]

> […] these functions don’t mutate the collection they’re called on; instead, they return a new collection with the updated data. As a result, it’s also common to chain them together in a “fluent” style to solve problems.

```scala
val nums = (1 to 10).toList   // List(1,2,3,4,5,6,7,8,9,10)

// methods can be chained together as needed
val x = nums.filter(_ > 3)
            .filter(_ < 7)
            .map(_ * 10)

// result: x == List(40, 50, 60)
```

### Traits
```scala
trait Animal:
  def speak(): Unit

trait HasTail:
  def wagTail(): Unit

class Dog extends Animal, HasTail:
  def speak(): Unit = println("Woof!")
  def wagTail(): Unit = println("⎞⎞⎛ ⎞⎛⎛")
```

Traits are not restricted to abstract members:[^35]

```scala
trait Walks:
  def numLegs: Int
  def walk(): Unit
  def stop() = println("Stopped walking")
```

Since Scala 3, traits can take parameters:

```scala
trait Pet(name: String):
  def greeting: String
  def age: Int
  override def toString = s"My name is $name, I say $greeting, and I’m $age"

class Dog(name: String, var age: Int) extends Pet(name):
  val greeting = "Woof"

val d = Dog("Fido", 1)
```

This partially eliminates the need for abstract classes, when the purpose is to "decompose and reuse behavior."[^36]

### Classes

```scala
// these parameters define a constructor
class Person(var firstName: String, var lastName: String):

  // initialization begins
  // this attribute is not part of the constructor
  val fullName = firstName + " " + lastName

  // a class method
  def printFullName: String =
    fullName

  // initialization ends
```

As with functions more generally, the arguments set in the constructor can have default values, which will create multiple alternate constructors:[^8]

```scala
class Socket(val timeout: Int = 5_000, val linger: Int = 5_000):
  override def toString = s"timeout: $timeout, linger: $linger"

val s = Socket()                  // timeout: 5000, linger: 5000
val s = Socket(2_500)             // timeout: 2500, linger: 5000
val s = Socket(10_000, 10_000)    // timeout: 10000, linger: 10000
val s = Socket(timeout = 10_000)  // timeout: 10000, linger: 5000
val s = Socket(linger = 10_000)   // timeout: 5000, linger: 10000
```

It's also possible to pass named parameters when instantiating a class:

```scala
val s = Socket(
  timeout = 10_000,
  linger = 10_000
)
```

### Case classes

> Case classes are used to model _immutable_ data structures.[^27]

By default, the case class attributes are public, immutable `val`s.

Using a case class has the benefit that the compiler will generate `apply` methods for class instantiation, an `unapply` method for [extractor pattern matching](https://scala-lang.org/files/archive/spec/3.4/08-pattern-matching.html#extractor-patterns), a `copy` method to create modified copies of an instance, `equals` and `hashCode` methods and a `toString` method.[^21]

```scala
// Case classes can be used as patterns
christina match
  case Person(n, r) => println("name is " + n)

// `equals` and `hashCode` methods generated for you
val hannah = Person("Hannah", "niece")
christina == hannah       // false

// `toString` method
println(christina)        // Person(Christina,niece)

// built-in `copy` method
case class BaseballTeam(name: String, lastWorldSeriesWin: Int)
val cubs1908 = BaseballTeam("Chicago Cubs", 1908)
val cubs2016 = cubs1908.copy(lastWorldSeriesWin = 2016)
// result:
// cubs2016: BaseballTeam = BaseballTeam(Chicago Cubs,2016)
```

`apply` allows class instantiation without the `new` keyword. It's placed in a generated companion object to the class:[^23]

```scala
object StringBuilder:
  inline def apply(s: String): StringBuilder = new StringBuilder(s)
  inline def apply(): StringBuilder = new StringBuilder()

// allows for this
builder = StringBuilder()
```

The `inline` keyword allows for compile-time optimizations that will replace calls to an inlined function with its body, meaning that code will get executed directly instead of through a function invocation. In this case, it will replace `StringBuilder.apply(…)` with `new StringBuilder(…)`.[^26]

See also:
- <https://docs.scala-lang.org/scala3/reference/metaprogramming/inline.html#>
- <https://docs.scala-lang.org/scala3/guides/macros/inline.html>

Case classes favor a functional paradigm as they are immutable, can be easily copied with changes, and have `unapply` methods for extracting their values.[^30]

### Objects and case objects

As case classes, **case objects** provide extra features such as serialization, a `hashCode` implementation and a `toString` implementation,[^32] as well as being usable with pattern matching expressions.[^31]

The Scala 2 documentation on case objects is more thorough in explaining what case objects are and how they differ from regular objects:

> you use a Scala `object` when you want to create a singleton object. As [the documentation states](https://docs.scala-lang.org/tour/singleton-objects.html), “Methods and values that aren’t associated with individual instances of a class belong in singleton objects, denoted by using the keyword `object` instead of `class`.”[^32]

```scala
object FileUtils {
    def readTextFileAsString(filename: String): Try[String] = ...
    def copyFile(srcFile: File, destFile: File): Try[Boolean] = ...
    def readFileToByteArray(file: File): Try[Array[Byte]] = ...
    def readFileToString(file: File): Try[String] = ...
    def readFileToString(file: File, encoding: String): Try[String] = ...
    def readLines(file: File, encoding: String): Try[List[String]] = ...
}
```

Case objects were also used to implement enums. Scala 3 introduced a native `enum` structure that provides compatibility with `java.lang.Enum`, unlike the previous usage of case objects in its place.[^33]

The example below, from the Scala 3 documentation, demonstrates how case objects can be used in pattern matching by mixing both case classes and case objects in a method body implemented through a match expression: 

```scala
sealed trait Message
case class PlaySong(name: String) extends Message
case class IncreaseVolume(amount: Int) extends Message
case class DecreaseVolume(amount: Int) extends Message
case object StopPlaying extends Message

def handleMessages(message: Message): Unit = message match
  case PlaySong(name)         => playSong(name)
  case IncreaseVolume(amount) => changeVolume(amount)
  case DecreaseVolume(amount) => changeVolume(-amount)
  case StopPlaying            => stopPlayingSong()
```

> you can write methods like this, which use pattern matching to handle the incoming message (assuming the methods `playSong`, `changeVolume`, and `stopPlayingSong` are defined somewhere else)[^31]

### Extractor patterns

Defining an `unapply` method allows matching extractor patterns:

```scala
class NameBased[A, B](a: A, b: B) {
  def isEmpty = false
  def get = this
  def _1 = a
  def _2 = b
}

object Extractor {
  def unapply(x: Any) = new NameBased(1, "two")
}

"anything" match {
  case Extractor(a, b) => println(s"\$a, \$b") //prints "1, two"
}
```

> `NameBased` is an extractor type for `NameBased` itself, since it has a member `isEmpty` returning a value of type Boolean, and it has a member `get` returning a value of type `NameBased`.[^28]

In this other example, custom `apply` and `unapply` methods are created for an object:

```scala
import scala.util.Random

object CustomerID {
  def apply(name: String) = s"$name--${Random.nextLong()}"
  def unapply(customerID: String): Option[String] = {
    val stringArray: Array[String] = customerID.split("--")
    if (stringArray.tail.nonEmpty) Some(stringArray.head) else None
  }
}

val customer1ID = CustomerID("Sukyoung")  // Sukyoung--23098234908
customer1ID match {
  case CustomerID(name) => println(name)  // prints Sukyoung
  case _ => println("Could not extract a CustomerID")
}
```

> The `apply` method creates a `CustomerID` string from a `name`. The `unapply` does the inverse to get the `name` back. When we call `CustomerID("Sukyoung")`, this is shorthand syntax for calling `CustomerID.apply("Sukyoung")`. When we call `case CustomerID(name) => println(name)`, we’re calling the unapply method with `CustomerID.unapply(customer1ID)`.[^29]

Extractors can be used to initialize a value definition, as the unapply method will return the matched value:

```scala
val customer2ID = CustomerID("Nico")
val CustomerID(name) = customer2ID
name  // Nico
```

> This is equivalent to `val name = CustomerID.unapply(customer2ID).get`.[^29]

In this example, it doesn't really matter if the actual instance exists:

```scala
val CustomerID(name2) = "--asdfasdfasdf"
name2 // ""

val CustomerID(name3) = "Jane-0"
name3 // Jane
```

It does matter though if the unapply code can run on the given string:

```scala
// unapply can't run because split("--") will fail
val CustomerID(name3) = "-asdfasdfasdf" // throws scala.MatchError
```

### Companion objects

A **companion object** is an object with the same name as a class, declared in the same file as its **companion class**.  It is able to access the private members of its companion, and allows separating attributes and methods that are not specific to instances:[^24]

```scala
class Circle(val radius: Double):
  def area: Double = Circle.calculateArea(radius)

object Circle:
  private def calculateArea(radius: Double): Double = Pi * pow(radius, 2.0)

val circle1 = Circle(5.0)
circle1.area
```

In the example above, each instance of `Circle` will have its own area, so the attribute `area` is defined in the class. The method `calculateArea` however is general for all instances, and can therefore be defined in the companion object.

> If `calculateArea` was public, it would be accessed as `Circle.calculateArea`[^25]

Companion objects can also contain `apply` and `unapply` methods:[^24]

```scala
class Person:
  var name = ""
  var age = 0
  override def toString = s"$name is $age years old"

object Person:

  // a one-arg factory method
  def apply(name: String): Person =
    var p = new Person
    p.name = name
    p

  // a two-arg factory method
  def apply(name: String, age: Int): Person =
    var p = new Person
    p.name = name
    p.age = age
    p

end Person

val joe = Person("Joe")
val fred = Person("Fred", 29)
```

### Pattern matching

```scala
import scala.annotation.switch

@switch val i = 2

val day = i match
  case 0 => "Sunday"
  case 1 => "Monday"
  case 2 => "Tuesday"
  case 3 => "Wednesday"
  case 4 => "Thursday"
  case 5 => "Friday"
  case 6 => "Saturday"
  case _ => "invalid day"   // the default, catch-all
```

> When writing simple `match` expressions like this, it’s recommended to use the `@switch` annotation on the variable `i`. This annotation provides a compile-time warning if the switch can’t be compiled to a `tableswitch` or `lookupswitch`, which are better for performance.[^19]

There are many different forms of patterns that can be used to write match expressions. Examples include:[^22]

```scala
def pattern(x: Matchable): String = x match

  // constant patterns
  case 0 => "zero"
  case true => "true"
  case "hello" => "you said 'hello'"
  case Nil => "an empty List"

  // sequence patterns
  case List(0, _, _) => "a 3-element list with 0 as the first element"
  case List(1, _*) => "list, starts with 1, has any number of elements"
  case Vector(1, _*) => "vector, starts w/ 1, has any number of elements"

  // tuple patterns
  case (a, b) => s"got $a and $b"
  case (a, b, c) => s"got $a, $b, and $c"

  // constructor patterns
  case Person(first, "Alexander") => s"Alexander, first name = $first"
  case Dog("Zeus") => "found a dog named Zeus"

  // type test patterns
  case s: String => s"got a string: $s"
  case i: Int => s"got an int: $i"
  case f: Float => s"got a float: $f"
  case a: Array[Int] => s"array of int: ${a.mkString(",")}"
  case as: Array[String] => s"string array: ${as.mkString(",")}"
  case d: Dog => s"dog: ${d.name}"
  case list: List[?] => s"got a List: $list"
  case m: Map[?, ?] => m.toString

  // the default wildcard pattern
  case _ => "Unknown"
```

```scala
val numAsString = i match
  case 1 | 3 | 5 | 7 | 9 => "odd"
  case 2 | 4 | 6 | 8 | 10 => "even"
  case _ => "too big"
```

```scala
def isTruthy(a: Matchable) = a match
  case 0 | "" => false
  case _ => true
```

```scala
val p = Person("Fred")

// later in the code
p match
  case Person(name) if name == "Fred" =>
    println(s"$name says, Yubba dubba doo")

  case Person(name) if name == "Bam Bam" =>
    println(s"$name says, Bam bam!")

  case _ => println("Watch the Flintstones!")
```

> There’s much more to pattern matching in Scala. Patterns can be nested, results of patterns can be bound, and pattern matching can even be user-defined. See the pattern matching examples in the [Control Structures chapter](https://docs.scala-lang.org/scala3/book/control-structures.html) for more details.[^7]

When matching a catch-all, it's possible to use the matched value in the body of the match case:

```scala
i match
  case 0 => println("1")
  case 1 => println("2")
  case what => println(s"You gave me: $what")
```

For this to work, the variable name on the left must be lowercase. If uppercase, a corresponding variable will be used from the scope:[^20]

```scala
val N = 42
val r = i match
  case 0 => println("1")
  case 1 => println("2")
  case N => println("42")
  case n => println(s"You gave me: $n" )

r // "42"
```

### String interpolation
```scala
println(s"2 + 2 = ${2 + 2}")   // "2 + 2 = 4"

val x = -1
println(s"x.abs = ${x.abs}")   // "x.abs = 1"
```

```scala
val a = "ll"
val b = ','
val c = "Scala"

println(s"He${a}o$b $c!") // Hello, Scala!
```

> The `s` that you place before the string is just one possible interpolator. If you use an `f` instead of an `s`, you can use `printf`-style formatting syntax in the string.[^4]

### Enums

> An **enumeration** can be used to define a type that consists of a finite set of named values. […] Basic enumerations are used to define sets of constants, like the months in a year, the days in a week, directions like north/south/east/west, and more.

```scala
enum CrustSize:
  case Small, Medium, Large

enum CrustType:
  case Thin, Thick, Regular

enum Topping:
  case Cheese, Pepperoni, BlackOlives, GreenOlives, Onions

import CrustSize.*
val currentCrustSize = Small
```

> Enum values can be compared using equals (`==`), and also matched on:

```scala
// if/then
if currentCrustSize == Large then
  println("You get a prize!")

// match
currentCrustSize match
  case Small => println("small")
  case Medium => println("medium")
  case Large => println("large")
```

> Enumerations can also be parameterized [and] have members (like fields and methods):[^37]

```scala
enum Planet(mass: Double, radius: Double):
  private final val G = 6.67300E-11
  def surfaceGravity = G * mass / (radius * radius)
  def surfaceWeight(otherMass: Double) =
    otherMass * surfaceGravity

  case Mercury extends Planet(3.303e+23, 2.4397e6)
  case Earth   extends Planet(5.976e+24, 6.37814e6)
  // more planets here ...
```

> If you want to use Scala-defined enums as Java enums, you can do so by extending the class `java.lang.Enum` (which is imported by default) as follows:[^38]

```scala
enum Color extends Enum[Color] { case Red, Green, Blue }
```

### Collections

#### Lists

> Here are some examples that use the `List` class, which is an immutable, linked-list class. These examples show different ways to create a populated `List`:[^13]

```scala
val a = List(1, 2, 3)           // a: List[Int] = List(1, 2, 3)

// Range methods
val b = (1 to 5).toList         // b: List[Int] = List(1, 2, 3, 4, 5)
val c = (1 to 10 by 2).toList   // c: List[Int] = List(1, 3, 5, 7, 9)
val e = (1 until 5).toList      // e: List[Int] = List(1, 2, 3, 4)
val f = List.range(1, 5)        // f: List[Int] = List(1, 2, 3, 4)
val g = List.range(1, 10, 3)    // g: List[Int] = List(1, 4, 7)
```

List methods:

```scala
val a = List(10, 20, 30, 40, 10)      // List(10, 20, 30, 40, 10)

a.drop(2)                             // List(30, 40, 10)
a.dropWhile(_ < 25)                   // List(30, 40, 10)
a.filter(_ < 25)                      // List(10, 20, 10)
a.slice(2,4)                          // List(30, 40)
a.tail                                // List(20, 30, 40, 10)
a.take(3)                             // List(10, 20, 30)
a.takeWhile(_ < 30)                   // List(10, 20)

// flatten
val a = List(List(1,2), List(3,4))
a.flatten                             // List(1, 2, 3, 4)

// map, flatMap
val nums = List("one", "two")
nums.map(_.toUpperCase)               // List("ONE", "TWO")
nums.flatMap(_.toUpperCase)           // List('O', 'N', 'E', 'T', 'W', 'O')

val firstTen = (1 to 10).toList            // List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
firstTen.reduceLeft(_ + _)                 // 55
firstTen.foldLeft(100)(_ + _)              // 155 (100 is a “seed” value)
```

#### Tuples

> […] a collection of different types in the same container. For example,

```scala
val t = (11, "eleven", Person("Eleven"))
```

> […] access its values by binding them to variables, 

```scala
val (num, str, person) = t

// result:
// val num: Int = 11
// val str: String = eleven
// val person: Person = Person(Eleven)
```

> or access them by number:

```scala
t(0)   // 11
t(1)   // "eleven"
t(2)   // Person("Eleven")
```

See also:
  - <https://scala-lang.org/api/3.x/scala/collection/immutable.html>
  - <https://scala-lang.org/api/3.x/scala/collection/mutable.html>

### Control structures

Control structures are expressions:

```scala
val x = if a < b then a else b
```

> Note that this really is an _expression_—not a statement. This means that it returns a value, so you can assign the result to a variable:

> An expression returns a result, while a statement does not. Statements are typically used for their side-effects, such as using `println` to print to the console.[^5]

#### Expression-oriented programming

> […] lines of code that don’t return values are called statements, and they’re used for their side-effects. For example, these [last two] lines of code don’t return values, so they’re used for their side effects:

```scala
val minValue = if a < b then a else b // expression
if a == b then action() // statement 
println("Hello") // statement
```

> The first example runs the `action` method as a side effect when `a` is equal to `b`. The second example is used for the side effect of printing a string to STDOUT. As you learn more about Scala you’ll find yourself writing more _expressions_ and fewer _statements_.[^18]

### Throw unimplemented
```scala
??? // throws NotImplementedError
```

## Domain Modelling

### Object-oriented programming

#### Traits

> Perhaps different from other languages with support for OOP, such as Java, the primary tool of decomposition in Scala is not classes, but traits. They can serve to describe abstract interfaces [and] can also contain concrete implementations:[^39]

```scala
trait Showable:
  def show: String
  def showHtml = "<p>" + show + "</p>"

class Document(text: String) extends Showable:
  def show = text
```

Above, the method `showHtml` is defined "_in terms_ of the abstract method `show`."[^39]

> Abstract methods are not the only thing that can be left abstract in a trait. A trait can contain:[^40]

- > abstract methods (`def m(): T`)
- > abstract value definitions (`val x: T`)
- > abstract type members (`type T`), potentially with bounds (`type T <: S`)
- > abstract givens (`given t: T`)

##### Mixin composition

```scala
trait GreetingService:
  def translate(text: String): String
  def sayHello = translate("Hello")

trait TranslationService:
  def translate(text: String): String = "..."

trait ComposedService extends GreetingService, TranslationService
```

> To compose the two services, we can simply create a new trait extending [both of] them. Abstract members in one trait (such as `translate` in `GreetingService`) are automatically matched with concrete members in another trait. This not only works with methods as in this example, but also with all the other abstract members mentioned above (that is, types, value definitions, etc.).[^41]

#### Classes

> At some point we’ll want to create instances of [traits]. When designing software in Scala, it’s often helpful to only consider using classes at the leafs of your inheritance model:

- Traits: `T1`, `T2`, `T3` 
- Composed traits: `S1 extends T1, T2`, `S2 extends T2, T3` 
- Classes: `C extends S1, T3` 
- Instances: `C()`

> This is even more the case in Scala 3, where traits now can also take parameters, further eliminating the need for classes.

> Like traits, classes can extend multiple traits (but only one super class):

```scala
class MyService(name: String) extends ComposedService, Showable:
  def show = s"$name says $sayHello"

val s1 = MyService("Service 1")
```

> Through the means of subtyping, [the] instance `s1` can be used everywhere that any of the extended traits is expected:[^42]

```scala
val s2: GreetingService = s1
val s3: TranslationService = s1
val s4: Showable = s1
```

##### Open classes
> It is possible to extend another class. However, since _traits_ are designed as the primary means of decomposition, it is not recommended to extend a class that is defined in one file from another file.

> In Scala 3 extending non-abstract classes in other files is restricted. In order to allow this, the base class needs to be marked as `open`:

```scala
open class Person(name: String)
```

##### Access modifiers

All member definitions are **public by default**:

```scala
class Counter:
  // can only be observed by the method `count`
  private var currentCount = 0

  def tick(): Unit = currentCount += 1
  def count: Int = currentCount
```

> To hide implementation details, it’s possible to define members [as] `private` or `protected`. This way you can control how they are accessed or overridden. Private members are only visible to the class/trait itself and to its companion object. Protected members are also visible to subclasses of the class.[^44]

> Having to explicitly mark classes as open avoids many common pitfalls in OO design. In particular, it requires library designers to explicitly plan for extension and for instance document [?] the classes that are marked as open with additional extension contracts.
[^43]

##### Auxiliary constructors

> You can define a class to have multiple constructors so consumers of your class can build it in different ways. For example, let’s assume that you need to write some code to model students in a college admission system. While analyzing the requirements you’ve seen that you need to be able to construct a `Student` instance in three ways:[^34]

```scala
import java.time.*

// [1] the primary constructor
class Student(
  var name: String,
  var govtId: String
):
  private var _applicationDate: Option[LocalDate] = None
  private var _studentId: Int = 0

  // [2] a constructor for when the student has completed
  // their application
  def this(
    name: String,
    govtId: String,
    applicationDate: LocalDate
  ) =
    this(name, govtId)
    _applicationDate = Some(applicationDate)

  // [3] a constructor for when the student is approved
  // and now has a student id
  def this(
    name: String,
    govtId: String,
    studentId: Int
  ) =
    this(name, govtId)
    _studentId = studentId

// [these] constructors can be called like this:
val s1 = Student("Mary", "123")
val s2 = Student("Mary", "123", LocalDate.now)
val s3 = Student("Mary", "123", 456)
```

#### Objects

> An object is a class that has exactly one instance. It’s initialized lazily when its members are referenced, similar to a `lazy val`. Objects in Scala allow grouping methods and fields under one namespace, similar to how you use `static` members on a class in Java, Javascript (ES6), or `@staticmethod` in Python.

> Declaring an `object` is similar to declaring a `class`. Here’s an example of a “string utilities” object that contains a set of methods for working with strings:[^9]

```scala
object StringUtils:
  def truncate(s: String, length: Int): String = s.take(length)
  def containsWhitespace(s: String): Boolean = s.matches(".*\\s.*")
  def isNullOrEmpty(s: String): Boolean = s == null || s.trim.isEmpty

StringUtils.truncate("Chuck Bartowski", 5)  // "Chuck"

import StringUtils.* 
// or `import StringUtils.{truncate, containsWhitespace, isNullOrEmpty}`

truncate("Chuck Bartowski", 5)       // "Chuck"
containsWhitespace("Sarah Walker")   // true
isNullOrEmpty("John Casey")          // false
```

> Objects can also contain fields, which are also accessed like static members:

```scala
object MathConstants:
  val PI = 3.14159
  val E = 2.71828

println(MathConstants.PI)   // 3.14159
```

> An `object` that has the same name as a class, and is declared in the same file as the class, is called a _“companion object_.” Similarly, the corresponding class is called the object’s companion class. A companion class or object can access the private members of its companion.

> Companion objects are used for methods and values that are not specific to instances of the companion class. For instance, in the following example the class `Circle` has a member named `area` which is specific to each instance, and its companion object has a method named `calculateArea` that’s (a) not specific to an instance, and (b) is available to every instance:[^10]

```scala
import scala.math.*

class Circle(val radius: Double):
  def area: Double = Circle.calculateArea(radius)

object Circle:
  private def calculateArea(radius: Double): Double = Pi * pow(radius, 2.0)

val circle1 = Circle(5.0)
circle1.area
```

> Objects can also be used to implement traits to create modules. This technique takes two traits and combines them to create a concrete `object`:

```scala
trait AddService:
  def add(a: Int, b: Int) = a + b

trait MultiplyService:
  def multiply(a: Int, b: Int) = a * b

// implement those traits as a concrete object
object MathService extends AddService, MultiplyService

// use the object
import MathService.*
println(add(1,1))        // 2
println(multiply(2,2))   // 4
```

### Functional programming

> **Functional Programming** is programming with functions. When we say functions, we mean mathematical functions, which are supposed to have the following properties:

- > **total** – defined for every input. For example, `String#toInt` is not total because it’s not defined for any `String` input like “foo”.
- > **deterministic** – the same input always gives the same output. `Random#nextInt(100)` is not deterministic because when we call it two times, we’re gonna get 2 different results.
- > **pure** – without side effects, with the only concern of computing the result. The side effects can be anything from console logging to calling external API.[^1]

----

> write what you want, not how to achieve it:[^2]

```scala
// imperative
import scala.collection.mutable.ListBuffer

def double(ints: List[Int]): List[Int] =
  val buffer = new ListBuffer[Int]()
  for i <- ints do
    buffer += i * 2
  buffer.toList

val oldNumbers = List(1, 2, 3)
val newNumbers = double(oldNumbers)

// functional
val newNumbers = oldNumbers.map(_ * 2)
```

#### Algebraic Data Types

Algebraic data types are types composed of other types. **Sum types** combine different and exclusive alternatives or branches, typically implemented using enums or traits. **Product types** are formed by combining multiple types into a single type, for example a tuple defined as `(String, Int)`.

See these minimal examples:

```scala
// sum type
enum Natural:
  case Zero
  case Successor(predecessor: Natural)

// product type
case class Person(name: String, age: Int)
```

In the examples above, a `Natural` type is one of either `Zero` or `Successor`. No two options, nor other types, will be accepted. In the product type, both a `String` and an `Int` type are combined.

They are called sum and product types because of how you can obtain the total number of values they can contain by either multiplying the parameters (for product types) or adding up the alternatives (for sum types).

In Scala, product types are typically defined using case classes:

```scala
case class WeatherForecast(latitude: Double, longitude: Double)
```

Considered as a function, the class above could be written as `(Double, Double) => WeatherForecast`, meaning it takes two doubles and returns a `WeatherForecast`. 

We can also say that it is a product type, since it is not an exclusive choice between alternatives but a composition of two doubles. 

Finally, because for each pair of doubles you can define a different `WeatherForecast`, we can also say that `WeatherForecast` is a Cartesian product between its arguments, as in `type WeatherForecast = Double × Double`, since this is the number of possible unique `WeatherForecast` definitions.[^11]

It is also possible to have a **hybrid type** when a sum type is comprised of many product types:

```scala
sealed trait Response // sum type
case class Valid(code: Int, body: String) extends Response // product type
case class Invalid(error: String, description: String) extends Response // product type
```

One advantage of using ADTs is that you have more control over what types, ranges or values are accepted by leveraging type systems and type definitions, rather than ad-hoc validation that must be repeated every time a value is passed into a function. This reduces the complexity of code and increases testability.

##### Product Types

> A product type is an algebraic data type (ADT) that only has one shape, for example a singleton object, represented in Scala by a `case` object; or an immutable structure with accessible fields, represented by a `case` class.

> A `case` class has all of the functionality of a `class`, and also has additional features baked in that make them useful for functional programming. When the compiler sees the `case` keyword in front of a `class` it has these effects and benefits:

- > Case class constructor parameters are public `val` fields by default, so the fields are immutable, and accessor methods are generated for each parameter.
- > An `unapply` method is generated, which lets you use case classes in more ways in `match` expressions.
- > A `copy` method is generated in the class. This provides a way to create updated copies of the object without changing the original object.
- `equals` and `hashCode` methods are generated to implement structural equality.
- > A default `toString` method is generated, which is helpful for debugging.

```scala
case class Person(
  name: String,
  vocation: String,
)

val p = Person("Reginald Kenneth Dwight", "Singer")
p // p: Person = Person(Reginald Kenneth Dwight,Singer)
p.name // Reginald Kenneth Dwight
p.name = "Joe" // error: can't assign a val field
val p2 = p.copy(name = "Elton John")
p2 // p2: Person = Person(Elton John,Singer)
```

## Testing

### Set shared context
```scala
import scala.concurrent.duration.Duration

abstract class BaseSuite extends munit.FunSuite {
  override val munitTimeout = Duration(10, "sec")
  val base = true
}

class Suite extends BaseSuite {
  test("base suite is extended") {
    assert(base)
  }
}
```

### Expect failure
```scala
test(".fail test fails".fail) {
  assertEquals(1, 0)
}
```

### Fixtures

> Test fixtures are the environments in which tests run. Fixtures allow you to acquire resources during setup and clean up resources after the tests finish running.

Types of fixtures:
- Functional test-local
- Reusable test-local
- Reusable suite-local
- Ad-hoc suite-local

#### Functional test-local fixtures

> [has] simple setup/teardown methods to initialize resources before a test case and clean up resources after a test case.

> Functional test-local fixtures are desirable since they are easy to reason about. Try to use functional test-local fixtures when possible, and only resort to reusable or ad-hoc fixtures when necessary.

```scala
import java.nio.file._

class FunFixture extends munit.FunSuite {
  val files = FunFixture[Path](
    setup = { test =>
      Files.createTempFile("tmp", test.name)
    },
    teardown = { file => 
      // Always gets called, even if test failed
      Files.deleteIfExists(file)
    }
  )

  files.test("basic") { file =>
    assert(Files.isRegularFile(file), s"Files.isRegularFile($file)")
  }
}
```

> Use `FunFixture.map2` to compose multiple fixtures into a single fixture.

```scala
// Fixture with access to two temporary files
val files2 = FunFixture.map2(files, files)
// files2: FunFixture[(Path, Path)] = munit.FunFixtures$FunFixture@4029121e9

files2.test("two") {
  case (file1, file2) =>
    assertNotEquals(file1, file2)
    assert(Files.isRegularFile(file1), s"Files.isRegularFile($file1)")
    assert(Files.isRegularFile(file2), s"Files.isRegularFile($file2)")
}
```

See [MUnit docs](https://scalameta.org/munit/docs/fixtures.html#reusable-test-local-fixtures) for Reusable test-local fixtures, Reusable suite-local fixtures and Ad-hoc suite-local fixtures.

### Avoiding stateful operations in the constructor
```scala
import java.sql.DriverManager
class MySuite extends munit.FunSuite {
  // Don't do this, because the class may get initialized even if no tests run.
  val db = DriverManager.getConnection("jdbc:h2:mem:", "sa", null)

  override def afterAll(): Unit = {
    // May never get called, resulting in connection leaking.
    db.close()
  }
}
```

> For example, IDEs like IntelliJ may load the class to discover the names of the test cases that are available.[^6]

### Clues

> Use `clue()` to include optional clues in the boolean condition based on values in the expression.

```scala
assert(clue(a) > clue(b))
```

Add this to `build.sbt` to prevent errors where the name of the variable is not shown in the clue:

```scala
scalacOptions += "-Yrangepos"
```

### Assertions

- `assert(bool)`
- `assertEquals(any, any)` 
  - > Comparing two values of different types is a compile error.
  - > It's a compile error even if the comparison is true at runtime.
  - > It's OK to compare two types as long as one argument is a subtype of the other type.
- `assertNotEquals(any, any)`
- `assertNoDiff(string, string)` compare two multiline strings
- `intercept(exception)` expect a particular exception to be thrown
- `interceptMessage(msg)` expect thrown exception to have a specific error message
- `fail()` fail the test immediately
- `compileErrors(string)` assert code snippet fails with a specific compile-time error message

## Tooling
### Suppress a WartRemover warning
```scala
@SuppressWarnings(Array("org.wartremover.warts.Var"))
var foo = "bar"
```

## Configuration

Suppress `info` level logging when running and watching runs:

`build.sbt`
```sbt
// ...
.settings(
  // ...
  logLevel := Level.Warn,
  run / watchLogLevel := Level.Warn,
)
```

## See also
- <https://scalac.io/blog/scala-isnt-hard-how-to-master-scala-step-by-step/>
- <https://scalameta.org/munit/docs/getting-started.html>
- <https://scalameta.org/munit/docs/assertions.html>
- <https://www.baeldung.com/scala/sbt-scoverage-code-analysis>
- <https://scala-cli.virtuslab.org/>
- <https://typelevel.org/cats/>
- <https://zio.dev/>

##  References
[^1]: <https://scalac.io/blog/why-use-scala/>
[^2]: <https://docs.scala-lang.org/scala3/book/scala-features.html>
[^3]: <https://docs.scala-lang.org/scala3/book/why-scala-3.html>
[^4]: <https://docs.scala-lang.org/scala3/book/taste-vars-data-types.html>
[^5]: <https://docs.scala-lang.org/scala3/book/taste-control-structures.html>
[^6]: <https://scalameta.org/munit/docs/fixtures.html>
[^7]: <https://docs.scala-lang.org/scala3/book/taste-control-structures.html#match-expressions>
[^8]: <https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html#default-parameter-values>
[^9]: <https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html#objects>
[^10]: <https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html#companion-objects>
[^11]: <https://youtu.be/0wmcCdoExbM>
[^12]: <https://docs.scala-lang.org/scala3/book/taste-methods.html>
[^13]: <https://docs.scala-lang.org/scala3/book/taste-collections.html>
[^14]: <https://docs.scala-lang.org/scala3/book/first-look-at-types.html>
[^15]: <https://docs.scala-lang.org/scala3/reference/experimental/explicit-nulls.html#>
[^16]: <https://docs.scala-lang.org/scala3/book/fp-functional-error-handling.html>
[^17]: <https://docs.scala-lang.org/scala3/book/string-interpolation.html>
[^18]: <https://docs.scala-lang.org/scala3/book/control-structures.html>
[^19]: <https://docs.scala-lang.org/scala3/book/control-structures.html#match-expressions>
[^20]: <https://docs.scala-lang.org/scala3/book/control-structures.html#using-the-default-value>
[^21]: <https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html#case-classes>
[^22]: <https://docs.scala-lang.org/scala3/book/control-structures.html#match-expressions-support-many-different-types-of-patterns>
[^23]: <https://docs.scala-lang.org/scala3/reference/other-new-features/creator-applications.html#>
[^24]: <https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html#companion-objects>
[^25]: <https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html#other-uses>
[^26]: <https://www.baeldung.com/scala/inline-modifier>
[^27]: <https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html#case-classes>
[^28]: <https://scala-lang.org/files/archive/spec/3.4/08-pattern-matching.html#extractor-patterns>
[^29]: <https://docs.scala-lang.org/tour/extractor-objects.html>
[^30]: <https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html#support-for-functional-programming>
[^31]: <https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html#case-objects>
[^32]: <https://docs.scala-lang.org/overviews/scala-book/case-objects.html#case-objects>
[^33]: <https://github.com/lampepfl/dotty/issues/1970>
[^34]: <https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html#auxiliary-constructors>
[^35]: <https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html#traits>
[^36]: <https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html#a-base-class-that-takes-constructor-arguments>
[^37]: <https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html#additional-enum-features>
[^38]: <https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html#compatibility-with-java-enums>
[^39]: <https://docs.scala-lang.org/scala3/book/domain-modeling-oop.html#traits>
[^40]: <https://docs.scala-lang.org/scala3/book/domain-modeling-oop.html#abstract-members>
[^41]: <https://docs.scala-lang.org/scala3/book/domain-modeling-oop.html#mixin-composition>
[^42]: <https://docs.scala-lang.org/scala3/book/domain-modeling-oop.html#subtyping>
[^43]: <https://docs.scala-lang.org/scala3/book/domain-modeling-oop.html#planning-for-extension>
[^44]: <https://docs.scala-lang.org/scala3/book/domain-modeling-oop.html#access-modifiers>
