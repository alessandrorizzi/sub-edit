name := "sub-edit"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies += "uk.co.caprica" % "vlcj" % "3.10.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1"

assemblyJarName in assembly := "sub-edit.jar"

mainClass in assembly := Some("re.rizzi.subedit.main.Main")

        
