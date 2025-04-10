package cc.unitmesh.sketch.indexer.usage

import cc.unitmesh.sketch.indexer.DomainDictService
import cc.unitmesh.sketch.llms.LlmFactory
import cc.unitmesh.sketch.sketch.ui.patch.readText
import cc.unitmesh.sketch.template.GENIUS_CODE
import cc.unitmesh.sketch.template.TemplateRender
import cc.unitmesh.sketch.template.context.TemplateContext
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile

@Service(Service.Level.PROJECT)
class PromptEnhancer(val project: Project) {
    val templateRender: TemplateRender get() = TemplateRender(GENIUS_CODE)
    val template = templateRender.getTemplate("enhance.vm")

    suspend fun create(input: String): String {
        val dict = project.getService(DomainDictService::class.java).loadContent() ?: ""
        val readme = readmeFile(project)
        val context = PromptEnhancerContext(dict, input, readme)
        val prompt = templateRender.renderTemplate(template, context)

        var result = StringBuilder()
        LlmFactory.create(project).stream(prompt, "").collect {
            result.append(it)
        }

        return result.toString()
    }

    companion object {
        private val README_VARIATIONS = listOf(
            "README.md", "Readme.md", "readme.md",
            "README.txt", "Readme.txt", "readme.txt",
            "README", "Readme", "readme"
        )
        
        fun findReadme(project: Project): VirtualFile? {
            val projectDir = project.guessProjectDir() ?: return null
            
            for (readmeVariation in README_VARIATIONS) {
                projectDir.findChild(readmeVariation)?.let { return it }
            }
            
            return null
        }

        fun readmeFile(project: Project): String {
            val readme = findReadme(project) ?: return ""
            return runCatching { readme.readText() }.getOrNull() ?: ""
        }
    }
}

data class PromptEnhancerContext(
    val dict: String,
    val userInput: String,
    val readme: String = "",
) : TemplateContext {

}
