package org.jetbrains.plugins.scala.externalHighlighters

import com.intellij.compiler.server.BuildManagerListener
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.externalHighlighters.compiler.DocumentCompiler

import java.util.UUID

private final class AutomakeBuildManagerListener extends BuildManagerListener {
  override def buildFinished(project: Project, sessionId: UUID, isAutomake: Boolean): Unit = {
    if (isAutomake && ScalaHighlightingMode.isShowErrorsFromCompilerEnabled(project)) {
      DocumentCompiler.get(project).clearOutputDirectories()
    }
  }
}
