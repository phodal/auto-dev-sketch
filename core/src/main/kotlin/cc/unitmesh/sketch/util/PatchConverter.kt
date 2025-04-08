package cc.unitmesh.sketch.util

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diff.impl.patch.TextFilePatch
import com.intellij.openapi.diff.impl.patch.apply.GenericPatchApplier
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.FileStatus
import com.intellij.openapi.vcs.VcsBundle
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ContentRevision
import com.intellij.openapi.vcs.changes.CurrentContentRevision
import com.intellij.openapi.vcs.changes.TextRevisionNumber
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcsUtil.VcsUtil
import org.jetbrains.annotations.NonNls
import java.io.File
import java.io.IOException

object PatchConverter {
    fun getAbsolutePath(baseDir: File, relativePath: String): File {
        var file: File?
        try {
            file = File(baseDir, relativePath).getCanonicalFile()
        } catch (e: IOException) {
            logger<PatchConverter>().info(e)
            file = File(baseDir, relativePath)
        }

        return file
    }

    fun createChange(project: Project, patch: TextFilePatch): Change {
        val baseDir = File(project.basePath!!)
        val beforePath = patch.beforeName
        val afterPath = patch.afterName

        val fileStatus = when {
            patch.isNewFile -> {
                FileStatus.ADDED
            }

            patch.isDeletedFile -> {
                FileStatus.DELETED
            }

            else -> {
                FileStatus.MODIFIED
            }
        }

        val beforeFilePath = VcsUtil.getFilePath(getAbsolutePath(baseDir, beforePath), false)
        val afterFilePath = VcsUtil.getFilePath(getAbsolutePath(baseDir, afterPath), false)

        var beforeRevision: ContentRevision? = null
        if (fileStatus !== FileStatus.ADDED) {
            beforeRevision = object : CurrentContentRevision(beforeFilePath) {
                override fun getRevisionNumber(): VcsRevisionNumber {
                    return TextRevisionNumber(VcsBundle.message("local.version.title"))
                }
            }
        }

        var afterRevision: ContentRevision? = null
        if (fileStatus !== FileStatus.DELETED) {
            afterRevision = object : CurrentContentRevision(beforeFilePath) {
                override fun getRevisionNumber(): VcsRevisionNumber =
                    TextRevisionNumber(VcsBundle.message("local.version.title"))

                override fun getVirtualFile(): VirtualFile? = afterFilePath.virtualFile
                override fun getFile(): FilePath = afterFilePath
                override fun getContent(): @NonNls String? {
                    when {
                        patch.isNewFile -> {
                            return patch.singleHunkPatchText
                        }

                        patch.isDeletedFile -> {
                            return null
                        }

                        else -> {
                            val localContent: String = loadLocalContent()
                            val appliedPatch = GenericPatchApplier.apply(localContent, patch.hunks)
                            /// sometimes llm will return a wrong patch which the content is not correct
                            if (appliedPatch != null) {
                                return appliedPatch.patchedText
                            }

                            return patch.singleHunkPatchText
                        }
                    }
                }

                @Throws(VcsException::class)
                private fun loadLocalContent(): String {
                    return ReadAction.compute<String?, VcsException?>(ThrowableComputable {
                        val file: VirtualFile? = beforeFilePath.virtualFile
                        if (file == null) return@ThrowableComputable null
                        val doc = FileDocumentManager.getInstance().getDocument(file)
                        if (doc == null) return@ThrowableComputable null
                        doc.text
                    })
                }
            }
        }

        return Change(beforeRevision, afterRevision, fileStatus)
    }

}