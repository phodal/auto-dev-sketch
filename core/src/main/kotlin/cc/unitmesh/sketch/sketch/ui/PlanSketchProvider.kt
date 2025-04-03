package cc.unitmesh.sketch.sketch.ui

import cc.unitmesh.sketch.observer.plan.MarkdownPlanParser
import cc.unitmesh.sketch.sketch.ui.code.CodeHighlightSketch
import cc.unitmesh.sketch.sketch.ui.plan.PlanLangSketch
import com.intellij.openapi.project.Project

class PlanSketchProvider : LanguageSketchProvider {
    override fun isSupported(lang: String): Boolean = lang == "plan"

    override fun create(project: Project, content: String): ExtensionLangSketch {
        val planItems = MarkdownPlanParser.parse(content)
        if (planItems.isNotEmpty()) {
            return PlanLangSketch(project, content, planItems.toMutableList())
        }

        return object : CodeHighlightSketch(project, content, null), ExtensionLangSketch {
            override fun getExtensionName(): String = "Plan"
        }
    }
}

