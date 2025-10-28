<<<<<<<< HEAD:exts/devins-lang/src/main/kotlin/cc/unitmesh/sketch/language/compiler/exec/LibraryVersionFetchInsCommand.kt
package cc.unitmesh.sketch.language.compiler.exec

import cc.unitmesh.sketch.provider.LibraryVersionProvider
import cc.unitmesh.sketch.provider.VersionRequest
import cc.unitmesh.sketch.command.InsCommand
import cc.unitmesh.sketch.command.dataprovider.BuiltinCommand
import cc.unitmesh.sketch.language.compiler.error.DEVINS_ERROR
import cc.unitmesh.sketch.provider.BuildSystemProvider
========
package cc.unitmesh.sketch.language.compiler.exec.idea

import cc.unitmesh.sketch.command.InsCommand
import cc.unitmesh.sketch.command.dataprovider.BuiltinCommand
import cc.unitmesh.sketch.language.compiler.error.DEVINS_ERROR
import cc.unitmesh.sketch.provider.BuildSystemProvider
import cc.unitmesh.sketch.provider.LibraryVersionProvider
import cc.unitmesh.sketch.provider.VersionRequest
import cc.unitmesh.sketch.provider.VersionResult
>>>>>>>> master:exts/devins-lang/src/main/kotlin/cc/unitmesh/devti/language/compiler/exec/idea/LibraryVersionFetchInsCommand.kt
import com.intellij.openapi.project.Project
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerializationException

class LibraryVersionFetchInsCommand(val myProject: Project, val prop: String, val codeContent: String) : InsCommand {
    override val commandName: BuiltinCommand = BuiltinCommand.LIBRARY_VERSION_FETCH

    override suspend fun execute(): String? {
        // Try to parse as JSON first, fallback to legacy string format
        val request = parseRequest(prop, codeContent)
        if (request == null) {
            return "$DEVINS_ERROR Invalid request format. Use JSON: {\"name\": \"react\", \"type\": \"npm\"} or legacy format: npm:react"
        }

        return fetchVersion(request)
    }

    private suspend fun fetchVersion(request: VersionRequest): String {
        // If type is specified, use it directly
        if (request.type != null) {
            val provider = LibraryVersionProvider.findProvider(request.type!!)
            if (provider != null) {
                val result = provider.fetchVersion(request)
                return formatResult(result)
            } else {
                return "$DEVINS_ERROR Unsupported package type: ${request.type}. Supported: ${LibraryVersionProvider.getSupportedTypes().joinToString(", ")}"
            }
        }

        // Auto-detect from project context
        return autoDetectAndFetch(request.name)
    }

<<<<<<<< HEAD:exts/devins-lang/src/main/kotlin/cc/unitmesh/sketch/language/compiler/exec/LibraryVersionFetchInsCommand.kt
    private fun formatResult(result: cc.unitmesh.sketch.provider.VersionResult): String {
========
    private fun formatResult(result: VersionResult): String {
>>>>>>>> master:exts/devins-lang/src/main/kotlin/cc/unitmesh/devti/language/compiler/exec/idea/LibraryVersionFetchInsCommand.kt
        return if (result.success) {
            result.version!!
        } else {
            "$DEVINS_ERROR ${result.error}"
        }
    }

    private fun parseRequest(prop: String, codeContent: String): VersionRequest? {
        // Try JSON format first
        if (codeContent.isNotBlank()) {
            try {
                return Json.decodeFromString<VersionRequest>(codeContent)
            } catch (e: SerializationException) {
                // Fallback to legacy format
            }
        }

        // Legacy string format: "type:name" or just "name"
        if (prop.isBlank()) return null

        val parts = prop.split(":", limit = 2)
        return if (parts.size == 2) {
            VersionRequest(name = parts[1].trim(), type = parts[0].trim())
        } else {
            VersionRequest(name = prop.trim(), type = null) // Auto-detect
        }
    }

    private suspend fun autoDetectAndFetch(packageName: String): String {
        val buildSystems = BuildSystemProvider.guess(myProject)
        val results = mutableListOf<String>()

        // Try different package managers based on project context
        val typesToTry = mutableSetOf<String>()
        buildSystems.forEach { context ->
            when {
                context.buildToolName?.lowercase()?.contains("npm") == true -> typesToTry.add("npm")
                context.buildToolName?.lowercase()?.contains("maven") == true -> typesToTry.add("maven")
                context.buildToolName?.lowercase()?.contains("gradle") == true -> typesToTry.add("maven")
                context.languageName?.lowercase()?.contains("javascript") == true -> typesToTry.add("npm")
                context.languageName?.lowercase()?.contains("typescript") == true -> typesToTry.add("npm")
                context.languageName?.lowercase()?.contains("java") == true -> typesToTry.add("maven")
                context.languageName?.lowercase()?.contains("kotlin") == true -> typesToTry.add("maven")
                context.languageName?.lowercase()?.contains("python") == true -> typesToTry.add("pypi")
                context.languageName?.lowercase()?.contains("go") == true -> typesToTry.add("go")
                context.languageName?.lowercase()?.contains("rust") == true -> typesToTry.add("crates")
            }
        }

        // If no specific context found, try common ones
        if (typesToTry.isEmpty()) {
            typesToTry.addAll(listOf("npm", "maven", "pypi"))
        }

        for (type in typesToTry) {
            val provider = LibraryVersionProvider.findProvider(type)
            if (provider != null) {
                val result = provider.fetchVersion(VersionRequest(packageName, type))
                if (result.success) {
                    results.add("$type: ${result.version}")
                }
            }
        }

        return if (results.isNotEmpty()) {
            "Library versions for '$packageName':\n${results.joinToString("\n")}"
        } else {
            "$DEVINS_ERROR No versions found for '$packageName' in any supported registry"
        }
    }




}
