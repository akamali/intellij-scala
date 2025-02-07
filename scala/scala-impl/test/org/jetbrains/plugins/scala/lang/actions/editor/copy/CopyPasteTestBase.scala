package org.jetbrains.plugins.scala.lang.actions.editor.copy

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.EditorTestUtil
import org.jetbrains.plugins.scala.base.ScalaLightCodeInsightFixtureTestAdapter
import org.jetbrains.plugins.scala.lang.formatting.settings.ScalaCodeStyleSettings
import org.jetbrains.plugins.scala.util.TypeAnnotationSettings

abstract class CopyPasteTestBase extends ScalaLightCodeInsightFixtureTestAdapter {
  protected val Start = EditorTestUtil.SELECTION_START_TAG
  protected val End = EditorTestUtil.SELECTION_END_TAG
  protected val Caret = EditorTestUtil.CARET_TAG

  protected val tab = "\t"
  protected val empty = ""

  val fromLangExtension: String = ".scala"

  private var oldSettings: ScalaCodeStyleSettings = _
  private var oldBlankLineSetting: Int = _

  override protected def setUp(): Unit = {
    super.setUp()

    val project = getProject
    oldSettings = ScalaCodeStyleSettings.getInstance(project)
    oldBlankLineSetting = oldSettings.BLANK_LINES_AROUND_METHOD_IN_INNER_SCOPES
    oldSettings.BLANK_LINES_AROUND_METHOD_IN_INNER_SCOPES = 0
    TypeAnnotationSettings.set(project, TypeAnnotationSettings.alwaysAddType(oldSettings))
  }

  override def tearDown(): Unit = {
    val project = getProject
    ScalaCodeStyleSettings.getInstance(project).BLANK_LINES_AROUND_METHOD_IN_INNER_SCOPES = oldBlankLineSetting
    TypeAnnotationSettings.set(project, oldSettings)
    super.tearDown()
  }

  protected def doTest(from: String, to: String, after: String): Unit = {
    def normalize(s: String): String = s.replace("\r", "")

    myFixture.configureByText(s"from.$fromLangExtension", normalize(from))
    myFixture.performEditorAction(IdeActions.ACTION_COPY)

    myFixture.configureByText("to.scala", normalize(to))
    myFixture.performEditorAction(IdeActions.ACTION_PASTE)

    myFixture.checkResult(normalize(after), true)
  }

  protected def doTestWithStrip(from: String, to: String, after: String): Unit = {
    doTest(from.stripMargin, to.stripMargin, after.stripMargin)
  }

  protected def doTestWithStripWithSelectedText(from: String, to: String, after: String, selectedText: String): Unit = {
    doTestWithStrip(from, to.replaceAll(Caret, selectedText), after)
  }

  protected def doTestWithStripWithAllSelections(from: String, to: String, after: String): Unit = {
    val selections = Seq(
      Caret,
      s"$Start$End",
      s"$Start  $End",
      s"$Start$tab$End",
      s"""$Start
         |$End""".stripMargin,
      s"""$Start$tab$empty
         |  $End""".stripMargin,
      s"${Start}print(1)$End",
      s"""$Start
         |  print(1)$tab$empty
         | $End""".stripMargin,
    )

    for (selectedText <- selections)
      doTestWithStripWithSelectedText(from, to, after, selectedText)
  }

  protected def doTestToEmptyFile(fromText: String, expectedText: String): Unit = {
    doTest(fromText, Caret, expectedText)
  }
}