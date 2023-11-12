# scala notes

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

Assignment:

```scala
val nums = List(1, 2, 3)
val p = Person("Martin", "Odersky") // not a built-in
```

Lambdas:
```scala
nums.map(i => i * 2) // long form
nums.map(_ * 2)      // short form

nums.filter(i => i > 1)
nums.filter(_ > 1)
```

Higher-order functions:
```scala
val xs = List(1, 2, 3, 4, 5)

xs.map(_ + 1)       // List(2, 3, 4, 5, 6)
xs.filter(_ < 3)    // List(1, 2)
xs.find(_ > 3)      // Some(4)
xs.takeWhile(_ < 3) // List(1, 2)
```

> In those examples, the values in the list can’t be modified. The List class is immutable, so all of those methods return new values, as shown by the data in each comment.[^3]

Traits and classes:
```scala
trait Animal:
  def speak(): Unit

trait HasTail:
  def wagTail(): Unit

class Dog extends Animal, HasTail:
  def speak(): Unit = println("Woof!")
  def wagTail(): Unit = println("⎞⎞⎛ ⎞⎛⎛")
```

Pattern matching:
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

String interpolation:
```scala
println(s"2 + 2 = ${2 + 2}")   // "2 + 2 = 4"

val x = -1
println(s"x.abs = ${x.abs}")   // "x.abs = 1"

val immutable = "Scala"
val a_char = ','
var mutable = "lo"
println(s"Hel${mutable}es$a_char $immutable!")
```

> The `s` that you place before the string is just one possible interpolator. If you use an `f` instead of an `s`, you can use `printf`-style formatting syntax in the string.[^4]

# Control structures

```scala
val x = if a < b then a else b
```

> Note that this really is an _expression_—not a statement. This means that it returns a value, so you can assign the result to a variable:

> An expression returns a result, while a statement does not. Statements are typically used for their side-effects, such as using `println` to print to the console.[^5]

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

##  References
[^1]: <https://scalac.io/blog/why-use-scala/>
[^2]: <https://docs.scala-lang.org/scala3/book/scala-features.html>
[^3]: <https://docs.scala-lang.org/scala3/book/why-scala-3.html>
[^4]: <https://docs.scala-lang.org/scala3/book/taste-vars-data-types.html>
[^5]: <https://docs.scala-lang.org/scala3/book/taste-control-structures.html>
