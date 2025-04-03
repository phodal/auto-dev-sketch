package cc.unitmesh.git.mcp

import cc.unitmesh.sketch.mcp.host.AbstractMcpTool
import cc.unitmesh.sketch.mcp.host.NoArgs
import cc.unitmesh.sketch.mcp.host.Response
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vfs.VirtualFile
import git4idea.history.GitHistoryUtils
import git4idea.repo.GitRepositoryManager
import kotlinx.serialization.Serializable
import java.nio.file.Path
import kotlin.io.path.Path

@Serializable
data class CommitQuery(val text: String)

class FindCommitByTextTool : AbstractMcpTool<CommitQuery>() {
    override val name: String = "find_commit_by_message"
    override val description: String = """
        Searches for a commit based on the provided text or keywords in the project history.
        Useful for finding specific change sets or code modifications by commit messages or diff content.
        Takes a query parameter and returns the matching commit information.
        Returns matched commit hashes as a JSON array.
    """

    override fun handle(project: Project, args: CommitQuery): Response {
        val queryText = args.text
        val matchingCommits = mutableListOf<String>()

        try {
            val vcs = ProjectLevelVcsManager.getInstance(project).allVcsRoots
                .mapNotNull { it.path }

            if (vcs.isEmpty()) {
                return Response("Error: No VCS configured for this project")
            }

            // Iterate over each VCS root to search for commits
            vcs.forEach { vcsRoot ->
                val repository = GitRepositoryManager.getInstance(project).getRepositoryForRoot(vcsRoot)
                    ?: return@forEach

                val gitLog = GitHistoryUtils.history(project, repository.root)

                gitLog.forEach { commit ->
                    if (commit.fullMessage.contains(queryText, ignoreCase = true)) {
                        matchingCommits.add(commit.id.toString())
                    }
                }
            }

            // Check if any matches were found
            return if (matchingCommits.isNotEmpty()) {
                Response(matchingCommits.joinToString(prefix = "[", postfix = "]", separator = ",") { "\"$it\"" })
            } else {
                Response("No commits found matching the query: $queryText")
            }

        } catch (e: Exception) {
            // Handle any errors that occur during the search
            return Response("Error while searching commits: ${e.message}")
        }
        return Response("Feature not yet implemented")
    }
}

class GetVcsStatusTool : AbstractMcpTool<NoArgs>() {
    override val name: String = "get_project_vcs_status"
    override val description: String = """
        Retrieves the current version control status of files in the project.
        Use this tool to get information about modified, added, deleted, and moved files in your VCS (e.g., Git).
        Returns a JSON-formatted list of changed files, where each entry contains:
        - path: The file path relative to project root
        - type: The type of change (e.g., MODIFICATION, ADDITION, DELETION, MOVED)
        Returns an empty list ([]) if no changes are detected or VCS is not configured.
        Returns error "project dir not found" if project directory cannot be determined.
        Note: Works with any VCS supported by the IDE, but is most commonly used with Git
    """

    override fun handle(project: Project, args: NoArgs): Response {
        val projectDir = project.guessProjectDir()?.toNioPathOrNull()
            ?: return Response(error = "project dir not found")

        val changeListManager = ChangeListManager.getInstance(project)
        val changes = changeListManager.allChanges

        return Response(changes.mapNotNull { change ->
            val absolutePath = change.virtualFile?.path ?: change.afterRevision?.file?.path
            val changeType = change.type

            if (absolutePath != null) {
                try {
                    val relativePath = projectDir.relativize(Path(absolutePath)).toString()
                    relativePath to changeType
                } catch (e: IllegalArgumentException) {
                    null  // Skip files outside project directory
                }
            } else {
                null
            }
        }.joinToString(",\n", prefix = "[", postfix = "]") {
            """{"path": "${it.first}", "type": "${it.second}"}"""
        })
    }
}

fun VirtualFile.toNioPathOrNull(): Path? {
    return runCatching { toNioPath() }.getOrNull()
}
