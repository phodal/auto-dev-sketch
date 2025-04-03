package cc.unitmesh.sketch.gui.snippet.container

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.intellij.json.JsonLanguage
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile

object AutoDevContainer {
    private val DEV_CONTAINER_PROPS =
        setOf("image", "dockerFile", "containerEnv", "remoteUser", "customizations", "features")

    fun updateForDevContainer(
        project: Project,
        lightVirtualFile: LightVirtualFile,
        content: String
    ): LightVirtualFile? {
        val fileName = lightVirtualFile.name.lowercase()
        if ((!content.startsWith("{") && !content.endsWith("}"))) return null

        if (fileName == "devcontainer.json" || fileName.contains("devcontainer")) {
            return lightVirtualFile
        }

        val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        val jsonNode: JsonNode
        try {
            jsonNode = objectMapper.readTree(content)
        } catch (e: Exception) {
            return null
        }

        if (!jsonNode.isObject) return null

        // Check if any dev container property exists
        val hasDevContainerProps = DEV_CONTAINER_PROPS.any { jsonNode.has(it) }
        if (!hasDevContainerProps) return null

        val image = jsonNode.path("image")
        val dockerfile = jsonNode.path("dockerFile")
        val remoteUser = jsonNode.path("remoteUser")

        val isDevContainer = when {
            !image.isMissingNode && image.isTextual && image.asText()
                .contains("mcr.microsoft.com/devcontainers") -> true

            !dockerfile.isMissingNode && dockerfile.isTextual -> true
            !remoteUser.isMissingNode && remoteUser.isTextual -> true
            jsonNode.size() >= 3 -> true
            else -> false
        }

        if (!isDevContainer) return null
        val newFile = LightVirtualFile("devcontainer.json", JsonLanguage.INSTANCE, content)

        return newFile
    }
}