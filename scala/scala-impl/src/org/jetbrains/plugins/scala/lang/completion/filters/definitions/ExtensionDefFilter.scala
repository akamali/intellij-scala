package org.jetbrains.plugins.scala
package lang
package completion
package filters.definitions

import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.{PsiComment, PsiElement, PsiErrorElement, PsiWhiteSpace}
import org.jetbrains.annotations.NonNls
import org.jetbrains.plugins.scala.extensions.{ObjectExt, PsiElementExt}
import org.jetbrains.plugins.scala.lang.completion.ScalaCompletionUtil._
import org.jetbrains.plugins.scala.lang.parser.ErrMsg
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScExtensionBody

class ExtensionDefFilter extends ElementFilter {
  override def isAcceptable(element: Any, context: PsiElement): Boolean = {
    if (context.is[PsiComment]) return false
    val (leaf, _) = processPsiLeafForFilter(getLeafByOffset(context.getTextRange.getStartOffset, context))

    if (leaf != null && leaf.getParent.is[ScExtensionBody]) {
      val errorBeforeDefStart = leaf.prevLeafs.filterNot(_.is[PsiComment, PsiWhiteSpace]).nextOption()

      errorBeforeDefStart match {
        case Some(err: PsiErrorElement) if err.getErrorDescription == ErrMsg("extension.method.expected") =>
          return true
        case _ =>
      }
    }

    false
  }

  override def isClassAcceptable(hintClass: Class[_]): Boolean = true

  @NonNls
  override def toString: String = "'def' keyword in extensions filter"
}
