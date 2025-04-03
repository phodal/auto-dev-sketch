package cc.unitmesh.database.flow

import cc.unitmesh.database.DbContextActionProvider
import cc.unitmesh.sketch.flow.TaskFlow
import cc.unitmesh.sketch.gui.chat.NormalChatCodingPanel
import cc.unitmesh.sketch.llms.LLMProvider
import cc.unitmesh.sketch.template.GENIUS_SQL
import cc.unitmesh.sketch.template.TemplateRender
import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.runBlocking

class AutoSqlFlow(
    private val genSqlContext: AutoSqlContext,
    private val actions: DbContextActionProvider,
    private val panel: NormalChatCodingPanel,
    private val llm: LLMProvider
) : TaskFlow<String> {
    private val logger = logger<AutoSqlFlow>()

    override fun clarify(): String {
        val stepOnePrompt = generateStepOnePrompt(genSqlContext, actions)

        panel.addMessage(stepOnePrompt, true, stepOnePrompt)

        return runBlocking {
            val prompt = llm.stream(stepOnePrompt, "")
            return@runBlocking panel.updateMessage(prompt)
        }
    }

    override fun design(context: Any): List<String> {
        val tableNames = context as List<String>
        val stepTwoPrompt = generateStepTwoPrompt(genSqlContext, actions, tableNames)

        panel.addMessage(stepTwoPrompt, true, stepTwoPrompt)

        return runBlocking {
            val prompt = llm.stream(stepTwoPrompt, "")
            return@runBlocking panel.updateMessage(prompt)
        }.let { listOf(it) }
    }

    private fun generateStepOnePrompt(context: AutoSqlContext, actions: DbContextActionProvider): String {
        val templateRender = TemplateRender(GENIUS_SQL)
        val template = templateRender.getTemplate("sql-gen-clarify.vm")

        templateRender.context = context
        templateRender.actions = actions

        val prompter = templateRender.renderTemplate(template)

        logger.info("AutoSQL step 1 flow: $prompter")
        return prompter
    }

    private fun generateStepTwoPrompt(
        genSqlContext: AutoSqlContext,
        actions: DbContextActionProvider,
        tableInfos: List<String>
    ): String {
        val templateRender = TemplateRender(GENIUS_SQL)
        val template = templateRender.getTemplate("sql-gen-design.vm")

        genSqlContext.tableInfos = actions.getTableColumns(tableInfos)

        templateRender.context = genSqlContext
        templateRender.actions = actions

        val prompter = templateRender.renderTemplate(template)

        logger.info("AutoSQL step 2 flow: $prompter")
        return prompter
    }

    override fun fix(errors: String): String {
        panel.addMessage(errors, true, errors)

        return runBlocking {
            val prompt = llm.stream(errors, "")
            return@runBlocking panel.updateMessage(prompt)
        }
    }

    fun getAllTables(): List<String> {
        return actions.dasTables.map { it.name }
    }
}