import scala.util.Properties

@main def hello: Unit =
  println(s"Scala Library version ${Properties.versionNumberString}")
  println(s"Development version: " +
    s"${Properties.developmentVersion.getOrElse("N/A")} " +
    s"Release version: ${Properties.releaseVersion.getOrElse("N/A")}"
  )
  println(
    s"${Properties.javaVmVendor} " +
    s"${Properties.javaVmName} " +
    s"${Properties.javaVmVersion} " +
    s"(${Properties.javaVmInfo})"
  )
  println(s"JDK Home: ${Properties.jdkHome}")
