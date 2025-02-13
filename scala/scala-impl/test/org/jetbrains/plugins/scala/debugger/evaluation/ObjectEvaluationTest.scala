package org.jetbrains.plugins.scala
package debugger
package evaluation

import org.junit.experimental.categories.Category

@Category(Array(classOf[DebuggerTests]))
class ObjectEvaluationTest_2_11 extends ObjectEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_11
}

@Category(Array(classOf[DebuggerTests]))
class ObjectEvaluationTest_2_12 extends ObjectEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_12
}

@Category(Array(classOf[DebuggerTests]))
class ObjectEvaluationTest_2_13 extends ObjectEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_13
}

@Category(Array(classOf[DebuggerTests]))
class ObjectEvaluationTest_3_0 extends ObjectEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_0

  override def testInnerClassObjectFromObject(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalStartsWith("localSSG", "InnerClassObjectFromObject$S$SS$G")
      evalStartsWith("localSSS", "InnerClassObjectFromObject$S$SS$S")
      evalStartsWith("S", "InnerClassObjectFromObject$S$SS$S")
      evalStartsWith("localSS", "InnerClassObjectFromObject$S$SS$")
    }
  }
}

@Category(Array(classOf[DebuggerTests]))
class ObjectEvaluationTest_3_1 extends ObjectEvaluationTest_3_0 {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_1
}

abstract class ObjectEvaluationTestBase extends ExpressionEvaluationTestBase {
  addSourceFile("SimpleObject.scala",
    s"""
       |object EvaluateObjects {
       |  def main(args: Array[String]): Unit = {
       |    println() $breakpoint
       |  }
       |}
       """.stripMargin.trim
  )
  addSourceFile("Simple.scala", "object Simple")
  addSourceFile("qual/Simple.scala",
    s"""
       |package qual
       |
       |object Simple
      """.stripMargin.trim
  )
  addSourceFile("qual/SimpleCaseClass.scala",
    s"""
       |package qual
       |
       |case class SimpleCaseClass()
      """.stripMargin.trim
  )
  addSourceFile("StableInner.scala",
    s"""
       |package qual
       |
       |object StableInner {
       |  object Inner
       |}
      """.stripMargin.trim
  )
  addSourceFile("qual/ClassInner.scala",
    s"""
       |package qual
       |
       |class ClassInner {
       |  object Inner
       |}
      """.stripMargin.trim
  )

  def testEvaluateObjects(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalStartsWith("Simple", "Simple$")
      evalStartsWith("qual.Simple", "qual.Simple$")
      evalStartsWith("collection.immutable.List", "scala.collection.immutable.List$")
      evalEquals("qual.SimpleCaseClass", "SimpleCaseClass")
      evalStartsWith("qual.StableInner.Inner", "qual.StableInner$Inner$")
      evalStartsWith("val x = new qual.ClassInner(); x.Inner", "qual.ClassInner$Inner$")
    }
  }

  addSourceFile("InnerClassObjectFromObject.scala",
    s"""
       |object InnerClassObjectFromObject {
       |  class S {
       |    object SS {
       |      object S {
       |        def foo(): Unit = {
       |          val localSS = SS
       |          val localSSS = SS.S
       |          val localSSG = SS.G
       |          println(localSS) $breakpoint
       |          println(localSSS)
       |          println(localSSG)
       |        }
       |      }
       |      object G
       |    }
       |    def foo(): Unit = {
       |      SS.S.foo()
       |    }
       |  }
       |
       |  def main(args: Array[String]): Unit = {
       |    val x = new S()
       |    x.foo()
       |  }
       |}
      """.stripMargin.trim
  )

  def testInnerClassObjectFromObject(): Unit = {
    expressionEvaluationTest() { implicit ctx =>
      evalStartsWith("SS.G", "InnerClassObjectFromObject$S$SS$G")
      evalStartsWith("SS.S", "InnerClassObjectFromObject$S$SS$S")
      evalStartsWith("S", "InnerClassObjectFromObject$S$SS$S")
      evalStartsWith("SS", "InnerClassObjectFromObject$S$SS$")
    }
  }
}
