package cc.unitmesh.sketch.custom.variable

import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import cc.unitmesh.sketch.inlay.chunks.SimilarChunksWithPaths

class SimilarChunkVariableResolver(val element: PsiElement) : VariableResolver {
    override val type: CustomResolvedVariableType get() = CustomResolvedVariableType.SIMILAR_CHUNK

    override fun resolve(): String {
        return try {
            ReadAction.compute<String, Throwable> {
                val chunks = SimilarChunksWithPaths.createQuery(element) ?: return@compute ""
                "```${element.language}\n$chunks\n```\n"
            }
        } catch (e: Throwable) {
            ""
        }
    }
}