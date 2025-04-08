package cc.unitmesh.sketch.language.compiler.exec

import cc.unitmesh.sketch.command.InsCommand
import cc.unitmesh.sketch.command.dataprovider.BuiltinCommand
import cc.unitmesh.sketch.language.utils.lookupFile
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project

class OpenInsCommand(val myProject: Project, private val filename: String) : InsCommand {
    override val commandName: BuiltinCommand = BuiltinCommand.OPEN

    override suspend fun execute(): String? {
        val file = myProject.lookupFile(filename)
        if (file != null) {
            FileEditorManager.getInstance(myProject).openFile(file, true)
        }

        return "Opening $filename..."
    }
}
