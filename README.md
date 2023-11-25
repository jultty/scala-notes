# scala notes

Notes on studying Scala, mainly from reading documentation such as:
- The [Scala Book](https://docs.scala-lang.org/scala3/book/introduction.html)
- The [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/)
- The [Munit docs](https://scalameta.org/munit/docs/getting-started.html)
- A few blogs and videos, see the [footnotes](#References) for all references

Aside from this README, the [tests](src/test/scala/Suite.scala) contain many runnable examples of some concepts explored here and many only explored there.

## Functional programming

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

## Basic syntax

### Assignment

```scala
val nums = List(1, 2, 3)
val p = Person("Martin", "Odersky") // not a built-in
```

### Types

All types inherit from the supertype `Any`. This top type has a subtype called `Matchable`, which comprehends all types that can be pattern matched.

The last these high-level, broader types explained in the "A First Look at Types" section of the Scala Book are the direct children of `Matchable`: `AnyVal` and `AnyRef`/`Object`.

From these two come the more specific types:

`Int | AnyVal` represents **non-nullable value types** such as `Double`, `Float`, `Long`, `Int`, `Short`, `Byte`, `Char`, `Unit`, and `Boolean`.

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

Even better than this, though, is to use something like [`Option`](https://scala-lang.org/api/3.x/scala/Option.html) to handle the possibilities in a more robust way.[^16]

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

### Traits and classes
```scala
trait Animal:
  def speak(): Unit

trait HasTail:
  def wagTail(): Unit

class Dog extends Animal, HasTail:
  def speak(): Unit = println("Woof!")
  def wagTail(): Unit = println("⎞⎞⎛ ⎞⎛⎛")
```

### Case class

Using a case class has the benefit that the compiler will generate an`unapply` method for pattern matching, a `copy` method to create modified copies of an instance, `equals` and `hashCode` methods, a `toString` method.[^21]

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

### Pattern matching
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

#### Pattern matching

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

### Domain Modelling

> Classes can also have methods and additional fields that are not part of constructors. They are defined in the body of the class. The body is initialized as part of the default constructor:[^8]

```scala
class Person(var firstName: String, var lastName: String):

  println("initialization begins")
  val fullName = firstName + " " + lastName

  // a class method
  def printFullName: Unit =
    // access the `fullName` field, which is created above
    println(fullName)

  printFullName
  println("initialization ends")
```

> class constructor parameters can also have default values:

```scala
class Socket(val timeout: Int = 5_000, val linger: Int = 5_000):
  override def toString = s"timeout: $timeout, linger: $linger"

val s = Socket()                  // timeout: 5000, linger: 5000
val s = Socket(2_500)             // timeout: 2500, linger: 5000
val s = Socket(10_000, 10_000)    // timeout: 10000, linger: 10000
val s = Socket(timeout = 10_000)  // timeout: 10000, linger: 5000
val s = Socket(linger = 10_000)   // timeout: 5000, linger: 10000
```

#### Auxiliary constructors

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
```

> An object is a class that has exactly one instance. It’s initialized lazily when its members are referenced, similar to a `lazy val`. Objects in Scala allow grouping methods and fields under one namespace, similar to how you use `static` members on a class in Java, Javascript (ES6), or `@staticmethod` in Python.

> Declaring an `object` is similar to declaring a `class`. Here’s an example of a “string utilities” object that contains a set of methods for working with strings:[^9]

```scala
object StringUtils:
  def truncate(s: String, length: Int): String = s.take(length)
  def containsWhitespace(s: String): Boolean = s.matches(".*\\s.*")
  def isNullOrEmpty(s: String): Boolean = s == null || s.trim.isEmpty

StringUtils.truncate("Chuck Bartowski", 5)  // "Chuck"

import StringUtils.* // or `import StringUtils.{truncate, containsWhitespace, isNullOrEmpty}`
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

**GPT:** _What is the advantage of defining a companion object in Scala instead of defining the companion object's attributes and methods in the class itself?_
  > The advantage of defining a companion object in Scala instead of defining
  the companion object's attributes and methods in the class itself is that
  the companion object can serve as a separate namespace for methods and
  attributes that are not specific to any instance of the class.

  > 1. Namespace separation: By defining a companion object, you can separate
  class-level methods and attributes from instance-level methods and attributes.
  This helps in organizing the code and makes it clearer to understand which
  methods are intended to be called on the class itself and which are meant to
  be called on instances of the class.
  > 2. Accessibility: Generally, the attributes and methods defined in a
  companion object are accessible directly on the class itself, without the
  need to create an instance of the class. This can be useful when you want to
  provide utility methods or access some shared state that is not tied to any
  specific instance.
  > 3. Static-like behavior: In Scala, the companion object is a singleton
  instance which allows you to define methods and attributes that can be
  accessed without instantiating the class. This provides a similar behavior
  to static methods and attributes in other languages.
  > 4. Initialization code: Companion objects can be used to define code that
  gets executed when the class is first accessed or when the companion object
  itself is loaded. This can be useful for initializing shared resources or
  performing some one-time setup.

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

#### Collections

##### Lists

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

##### Tuples

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

### Throw unimplemented
```scala
// ??? // yes, ???
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
[^8]: <https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html>
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
