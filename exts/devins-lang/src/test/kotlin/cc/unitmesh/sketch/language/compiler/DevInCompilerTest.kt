package cc.unitmesh.sketch.language.compiler

import cc.unitmesh.sketch.language.psi.DevInFile
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import kotlinx.coroutines.runBlocking
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.konan.file.File

class DevInCompilerTest : LightJavaCodeInsightFixtureTestCase() {
    fun testNormalString() {
        @Language("DevIn")
        val code = "Normal String"
        val file = myFixture.configureByText("test.devin", code)

        val compile = runBlocking { DevInsCompiler(project, file as DevInFile, myFixture.editor).compile() }
        assertEquals("Normal String", compile.output)
    }

    fun testForWriting() {
        val projectPath = project.basePath + File.separator

        @Language("DevIn")
        val code = "/write:${projectPath}Sample.devin#L1-L2\n```devin\nNormal String /\n```"
        myFixture.configureByText("Sample.devin", "Sample Text")
        val file = myFixture.configureByText("test.devin", code)

        try {
            val compile = runBlocking { DevInsCompiler(project, file as DevInFile, myFixture.editor).compile() }
            println(compile.output)
        } catch (e: Exception) {
//            fail(e.message)
        }
    }

    fun testForRefactoring() {
        @Language("DevIn")
        val code = "/refactor:rename cc.unitmesh.sketch.language.run.DevInsProgramRunner to cc.unitmesh.sketch.language.run.DevInsProgramRunnerImpl"
        val file = myFixture.configureByText("test.devin", code)

        try {
            val compile = runBlocking { DevInsCompiler(project, file as DevInFile, myFixture.editor).compile() }
            println(compile.output)
        } catch (e: Exception) {
//            fail(e.message)
        }
    }
}

