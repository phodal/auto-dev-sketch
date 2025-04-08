package cc.unitmesh.idea.provider

import cc.unitmesh.sketch.gui.chat.message.ChatActionType
import cc.unitmesh.sketch.prompting.code.TechStack
import cc.unitmesh.sketch.provider.context.ChatContextItem
import cc.unitmesh.sketch.provider.context.ChatContextProvider
import cc.unitmesh.sketch.provider.context.ChatCreationContext
import cc.unitmesh.sketch.util.relativePath
import cc.unitmesh.idea.context.library.LibraryDescriptor
import cc.unitmesh.idea.context.library.SpringLibrary
import com.intellij.openapi.externalSystem.model.project.LibraryData
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.project.Project
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.plugins.gradle.util.GradleConstants

open class SpringGradleContextProvider : ChatContextProvider {
    override fun isApplicable(project: Project, creationContext: ChatCreationContext): Boolean {
        return hasProjectLibraries(project) && creationContext.action != ChatActionType.CODE_COMPLETE
    }

    private fun hasProjectLibraries(project: Project): Boolean {
        prepareLibraryData(project)?.forEach {
            if (it.groupId?.contains("org.springframework") == true) {
                return true
            }
        }

        return false
    }

    override suspend fun collect(project: Project, creationContext: ChatCreationContext): List<ChatContextItem> {
        val techStacks = convertTechStack(project)

        if (techStacks.coreFrameworks().isEmpty() && techStacks.testFrameworks().isEmpty()) {
            return emptyList()
        }

        val fileName = creationContext.sourceFile?.name ?: ""
        val formattedTechStacks = techStacks.coreFrameworks.keys.joinToString(",")

        val configFile = SpringFrameworkConfigProvider().collect(project).map {
            it.relativePath(project)
        }

        val baseMessages = buildContextMessage(fileName, formattedTechStacks, configFile)
        return listOf(ChatContextItem(SpringGradleContextProvider::class, baseMessages))
    }

    private fun buildContextMessage(fileName: String, techStacks: String, configFiles: List<String>): String {
        val contextType = when {
            isControllerFile(fileName) -> "RESTful APIs"
            isServiceFile(fileName) -> "business logic"
            else -> "business logic"
        }
        
        return "You are working on a project that uses $techStacks to build $contextType, configured with files: ${configFiles.joinToString(",")}."
    }

    private fun isControllerFile(fileName: String): Boolean {
        return fileName.endsWith("Controller.java") || fileName.endsWith("Controller.kt")
    }

    private fun isServiceFile(fileName: String): Boolean {
        return fileName.endsWith("Service.java") || fileName.endsWith("ServiceImpl.java") ||
                fileName.endsWith("Service.kt") || fileName.endsWith("ServiceImpl.kt")
    }
}

fun convertTechStack(project: Project): TechStack {
    val libraryDataList = prepareLibraryData(project)

    val techStack = TechStack()
    var hasMatchSpringMvc = false
    var hasMatchSpringData = false
    var hasMatchSpringCloud = false

    libraryDataList?.forEach {
        val name = it.groupId + ":" + it.artifactId
        if (name.startsWith("org.springframework.boot")) {
            techStack.coreFrameworks.putIfAbsent("Spring Boot " + it.version, true)
        }

        if (!hasMatchSpringCloud) {
            if (name.startsWith("org.springframework.cloud")) {
                techStack.coreFrameworks.putIfAbsent("Spring Cloud " + it.version, true)
                hasMatchSpringCloud = true
            }
        }

        if (!hasMatchSpringMvc) {
            SpringLibrary.SPRING_MVC.forEach { entry: LibraryDescriptor ->
                if (name.contains(entry.coords)) {
                    techStack.coreFrameworks.putIfAbsent(entry.shortText, true)
                    hasMatchSpringMvc = true
                }
            }
        }

        if (!hasMatchSpringData) {
            SpringLibrary.SPRING_DATA.forEach { entry ->
                entry.coords.forEach { coord ->
                    if (name.contains(coord)) {
                        techStack.coreFrameworks.putIfAbsent(entry.shortText, true)
                        hasMatchSpringData = true
                    }
                }
            }
        }

        when {
            name.contains("org.springframework.boot:spring-boot-test") -> {
                techStack.testFrameworks.putIfAbsent("Spring Boot Test", true)
            }

            name.contains("org.assertj:assertj-core") -> {
                techStack.testFrameworks.putIfAbsent("AssertJ", true)
            }

            name.contains("org.junit.jupiter:junit-jupiter") -> {
                techStack.testFrameworks.putIfAbsent("JUnit 5", true)
            }

            name.contains("org.mockito:mockito-core") -> {
                techStack.testFrameworks.putIfAbsent("Mockito", true)
            }

            name.contains("com.h2database:h2") -> {
                techStack.testFrameworks.putIfAbsent("H2", true)
            }
        }
    }

    return techStack
}

data class SimpleLibraryData(val groupId: String?, val artifactId: String?, val version: String?)

fun prepareLibraryData(project: Project): List<SimpleLibraryData>? {
    return prepareGradleLibrary(project) ?: prepareMavenLibrary(project)
}

fun prepareGradleLibrary(project: Project): List<SimpleLibraryData>? {
    val basePath = project.basePath ?: return null
    val projectData = ProjectDataManager.getInstance().getExternalProjectData(
        project, GradleConstants.SYSTEM_ID, basePath
    )

    val libraryDataList: List<LibraryData>? = projectData?.externalProjectStructure?.children?.filter {
        it.data is LibraryData
    }?.map {
        it.data as LibraryData
    }

    return libraryDataList?.map {
        SimpleLibraryData(it.groupId, it.artifactId, it.version)
    }
}

fun prepareMavenLibrary(project: Project): List<SimpleLibraryData> {
    val projectDependencies: List<org.jetbrains.idea.maven.model.MavenArtifact> =
        MavenProjectsManager.getInstance(project).projects.flatMap {
            it.dependencies
        }

    return projectDependencies.map {
        SimpleLibraryData(it.groupId, it.artifactId, it.version)
    }
}
