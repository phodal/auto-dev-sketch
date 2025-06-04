package cc.unitmesh.ide.javascript.util

import com.intellij.javascript.testing.JSTestRunnerManager
import com.intellij.openapi.application.runReadAction
import com.intellij.psi.PsiFile

object JsUtil {
    fun guessTestFrameworkName(file: PsiFile): String? {
        val findPackageDependentProducers =
            runReadAction { JSTestRunnerManager.getInstance().findPackageDependentProducers(file) }

        val testRunConfigurationProducer = findPackageDependentProducers.firstOrNull()
        return testRunConfigurationProducer?.let {
            try {
                val configurationTypeField = it.javaClass.getDeclaredMethod("getConfigurationType")
                configurationTypeField.isAccessible = true
                val configurationType = configurationTypeField.invoke(it)
                configurationType?.javaClass?.getDeclaredMethod("getDisplayName")?.invoke(configurationType) as? String
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}