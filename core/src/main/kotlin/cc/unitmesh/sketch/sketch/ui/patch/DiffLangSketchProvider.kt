package cc.unitmesh.sketch.sketch.ui.patch

import cc.unitmesh.sketch.sketch.ui.ExtensionLangSketch
import cc.unitmesh.sketch.sketch.ui.LanguageSketchProvider
import com.intellij.openapi.project.Project

class DiffLangSketchProvider : LanguageSketchProvider {
    override fun isSupported(lang: String): Boolean = lang == "diff" || lang == "patch"
    override fun create(project: Project, content: String): ExtensionLangSketch {
        val contentWithoutEnding = if (content.endsWith("\n+```")) {
            content.substring(0, content.length - 4)
        } else {
            content
        }

        return DiffLangSketch(project, contentWithoutEnding)
    }
}
