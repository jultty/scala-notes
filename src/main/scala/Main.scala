import scala.util.Properties

@main def hello: Unit =
  print_environment_properties

def print_environment_properties: Unit =
  println(s"Scala Library version ${Properties.versionNumberString}")
  println(
    s"${Properties.javaVmVendor} " +
    s"${Properties.javaVmName} " +
    s"${Properties.javaVmVersion} " +
    s"(${Properties.javaVmInfo})"
  )
  println(s"JDK Home: ${Properties.jdkHome}");
