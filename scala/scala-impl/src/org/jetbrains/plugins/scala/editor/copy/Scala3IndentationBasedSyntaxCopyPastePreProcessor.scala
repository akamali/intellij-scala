package org.jetbrains.plugins.scala.editor.copy

import com.intellij.codeInsight.editorActions.CopyPastePreProcessor
import com.intellij.openapi.editor.{Editor, RawText}
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiFile, PsiWhiteSpace}
import org.jetbrains.plugins.scala.editor.Scala3IndentationBasedSyntaxUtils.{getIndentWhitespace, getLineIndentWhitespace}
import org.jetbrains.plugins.scala.editor.ScalaEditorUtils.findElementAtCaret_WithFixedEOF
import org.jetbrains.plugins.scala.extensions.{ObjectExt, PsiElementExt, PsiFileExt}

class Scala3IndentationBasedSyntaxCopyPastePreProcessor extends CopyPastePreProcessor {
  override def preprocessOnCopy(file: PsiFile, startOffsets: Array[Int], endOffsets: Array[Int], text: String): String = {
    // TODO make this work for multiple selections
    // TODO what about negative indents
    // if multi-line string, strip first-line indentation from all lines
    if (file.useIndentationBasedSyntax && startOffsets.length == 1 && text.contains('\n')) {
      var firstElement = file.findElementAt(startOffsets(0)).toOption
      file.getTextRange
      while (firstElement.exists(_.isWhitespace))
        firstElement = firstElement.get.nextVisibleLeaf
      if (firstElement.nonEmpty) {
        val leadingSpaceOnLine = getIndentWhitespace(firstElement.get)
        text.concat("\n").linesIterator.map(_.stripPrefix(leadingSpaceOnLine)).mkString("\n")
      } else null
    } else null
  }

  override def preprocessOnPaste(project: Project, file: PsiFile, editor: Editor, text: String, rawText: RawText): String = {
    // TODO make this work for multiple carets
    // if multi-line string and caret on new line, add caret indentation to all lines
    if (file.useIndentationBasedSyntax && text.contains('\n')) {
      val caret = editor.getCaretModel.getCurrentCaret
      val elementAtCaret = findElementAtCaret_WithFixedEOF(file, editor.getDocument, caret.getSelectionStart)
      val indentWhitespace = elementAtCaret match {
        case null => ""
        case ws: PsiWhiteSpace =>
          var wsText = ws.getText
          wsText = wsText.substring(0, caret.getSelectionStart - ws.startOffset)
          if (wsText.contains('\n'))
            wsText.substring(wsText.lastIndexOf('\n') + 1)
          else ""
        case el => getLineIndentWhitespace(el)
      }
      text.linesIterator.map(indentWhitespace + _).mkString("\n").stripLeading()
    } else text
  }
}
