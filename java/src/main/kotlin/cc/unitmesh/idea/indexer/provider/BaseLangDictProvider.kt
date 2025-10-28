package cc.unitmesh.idea.indexer.provider

import cc.unitmesh.sketch.indexer.model.DomainDictionary
import cc.unitmesh.sketch.indexer.model.SemanticName
import cc.unitmesh.sketch.indexer.naming.LanguageSuffixRules
import cc.unitmesh.sketch.indexer.provider.LangDictProvider
import cc.unitmesh.sketch.vcs.context.TokenCounter
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod

/**
 * Base class for language-specific dictionary providers.
 * Implements the two-level collection strategy with weight calculation:
 * - Level 1: Filenames with weights (low token cost, basic domain understanding)
 * - Level 2: Class names and public method names with weights (medium token cost, better domain understanding)
 *
 * Subclasses only need to provide language-specific rules and PSI element extraction.
 */
abstract class BaseLangDictProvider : LangDictProvider {
    protected val tokenCounter = TokenCounter.DEFAULT

    /**
     * Get language-specific suffix rules (e.g., remove "Controller", "Service" for Java)
     */
    protected abstract fun getSuffixRules(): LanguageSuffixRules

    /**
     * Get language-specific file filter
     */
    protected abstract fun shouldIncludeFile(fileName: String, filePath: String): Boolean

    /**
     * Extract classes and methods from a single Java file
     */
    protected open fun extractClassesAndMethods(
        javaFile: PsiJavaFile
    ): Pair<List<PsiClass>, List<PsiMethod>> {
        val classes = javaFile.classes.toList()
        val methods = classes.flatMap { getPublicMethods(it) }
        return Pair(classes, methods)
    }

    protected open fun getPublicMethods(psiClass: PsiClass): List<PsiMethod> = psiClass.methods.toList()

    protected open fun getPackageName(psiClass: PsiClass): String =
        (psiClass.containingFile as? PsiJavaFile)?.packageName ?: ""

    /**
     * Collect semantic names in two levels based on token budget with weights
     */
    override suspend fun collectFileNames(project: Project, maxTokenLength: Int): List<String> {
        // For backward compatibility, return Level 1 only
        return collectLevel1(project).map { it.name }
    }

    override suspend fun collectSemanticNames(
        project: Project,
        maxTokenLength: Int
    ): DomainDictionary {
        val level1 = collectLevel1(project)
        val level1Tokens = level1.sumOf { it.tokens }

        // If Level 1 uses less than 50% of budget, collect Level 2
        val level2 = if (level1Tokens < maxTokenLength * 0.5) {
            collectLevel2(project, maxTokenLength - level1Tokens)
        } else {
            emptyList()
        }

        val metadata = mapOf(
            "level1_count" to level1.size,
            "level2_count" to level2.size,
            "total_tokens" to (level1Tokens + level2.sumOf { it.tokens }),
            "max_tokens" to maxTokenLength
        )

        return DomainDictionary(level1, level2, metadata)
    }

    abstract suspend fun collectLevel1(project: Project): List<SemanticName>
    abstract suspend fun collectLevel2(
        project: Project,
        remainingTokenBudget: Int
    ): List<SemanticName>
}
