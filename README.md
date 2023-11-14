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

### Assignment

```scala
val nums = List(1, 2, 3)
val p = Person("Martin", "Odersky") // not a built-in
```

### Lambdas
```scala
nums.map(i => i * 2) // long form
nums.map(_ * 2)      // short form

nums.filter(i => i > 1)
nums.filter(_ > 1)
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
```scala
final case class Person(
  name: String,
  surname: String,
)

def p(name: String, surname: String): Person =
  Person(name, surname)

val jane = p("Jane", "Doe")
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

This can now be used to compose multiple fixtures into a single one:

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

#### Reusable test-local fixtures
#### Reusable suite-local fixtures
#### Ad-hoc suite-local fixtures

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

##  References
[^1]: <https://scalac.io/blog/why-use-scala/>
[^2]: <https://docs.scala-lang.org/scala3/book/scala-features.html>
[^3]: <https://docs.scala-lang.org/scala3/book/why-scala-3.html>
[^4]: <https://docs.scala-lang.org/scala3/book/taste-vars-data-types.html>
[^5]: <https://docs.scala-lang.org/scala3/book/taste-control-structures.html>
[^6]: <https://scalameta.org/munit/docs/fixtures.html>
