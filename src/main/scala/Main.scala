@main def hello: Unit =
  val a_char = ','
  val immutable = "Scala"
  var mutable = "lo"
  println(s"Hel${mutable}es$a_char $immutable!")
