package cc.unitmesh.sketch.actions

import cc.unitmesh.sketch.custom.tasks.FileGenerateTask
import cc.unitmesh.sketch.AutoDevBundle
import cc.unitmesh.sketch.provider.BuildSystemProvider
import cc.unitmesh.sketch.template.GENIUS_CICD
import cc.unitmesh.sketch.template.TemplateRender
import cc.unitmesh.sketch.actions.context.DevOpsContext
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.guessProjectDir
import kotlin.io.path.createDirectories

class GenerateGitHubActionsAction : AnAction(AutoDevBundle.message("action.new.genius.cicd.github")) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        try {
            val buildSystem = BuildSystemProvider.guess(project)
            val templateRender = TemplateRender(GENIUS_CICD)
            templateRender.context = DevOpsContext.from(buildSystem)
            val template = templateRender.getTemplate("generate-github-action.vm")

            val projectDir = project.guessProjectDir()?.toNioPath()
                ?: throw IllegalStateException("Cannot determine project directory")

            val workflowDir = projectDir.resolve(".github").resolve("workflows")
            workflowDir.createDirectories()

            val msgs = templateRender.buildMsgs(template)
            val task: Task.Backgroundable = FileGenerateTask(project, msgs, "ci.yml")

            ProgressManager.getInstance()
                .runProcessWithProgressAsynchronously(task, BackgroundableProcessIndicator(task))

        } catch (e: Exception) {
            logger<GenerateGitHubActionsAction>().error("Failed to generate GitHub Actions workflow", e)
        }
    }
}


