package cc.unitmesh.sketch.language.compiler.exec

import cc.unitmesh.sketch.command.InsCommand
import cc.unitmesh.sketch.command.dataprovider.BuiltinCommand
import cc.unitmesh.sketch.language.compiler.error.DEVINS_ERROR
import cc.unitmesh.sketch.sketch.rule.ProjectRule
import com.intellij.openapi.project.Project

class RuleInsCommand(val myProject: Project, private val filename: String) : InsCommand {
    override val commandName: BuiltinCommand = BuiltinCommand.OPEN

    override suspend fun execute(): String? {
        val projectRule = ProjectRule(myProject)
        return projectRule.getRuleContent(filename) ?: "$DEVINS_ERROR rule file not found: $filename"
    }
}
