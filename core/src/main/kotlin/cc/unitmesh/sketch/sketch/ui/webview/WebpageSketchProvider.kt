package cc.unitmesh.sketch.sketch.ui.webview

import cc.unitmesh.sketch.sketch.ui.ExtensionLangSketch
import cc.unitmesh.sketch.sketch.ui.LanguageSketchProvider
import cc.unitmesh.sketch.sketch.ui.code.HtmlHighlightSketch
import com.intellij.openapi.project.Project

class WebpageSketchProvider : LanguageSketchProvider {
    override fun isSupported(lang: String): Boolean {
        return lang.lowercase() == "html" || lang.lowercase() == "htm"
    }

    override fun create(project: Project, content: String): ExtensionLangSketch {
        if (content.startsWith("<!DOCTYPE html>") || content.startsWith("<html>")) {
            return WebpageLangSketch(project, content)
        }

        return HtmlHighlightSketch(project, content)
    }
}

