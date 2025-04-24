<<<<<<<< HEAD:exts/devins-lang/src/test/kotlin/cc/unitmesh/sketch/language/DevInParsingTest.kt
package cc.unitmesh.sketch.language
========
package cc.unitmesh.sketch.language.parse
>>>>>>>> master:exts/devins-lang/src/test/kotlin/cc/unitmesh/devti/language/parse/DevInParsingTest.kt

import cc.unitmesh.sketch.language.parser.DevInParserDefinition
import com.intellij.testFramework.ParsingTestCase

class DevInParsingTest : ParsingTestCase("parser", "devin", DevInParserDefinition()) {
    override fun getTestDataPath(): String {
        return "src/test/testData"
    }

    fun testBasicTest() {
        doTest(true)
    }

    fun testJavaHelloWorld() {
        doTest(true)
    }

    fun testEmptyCodeFence() {
        doTest(true)
    }

    fun testJavaAnnotation() {
        doTest(true)
    }

    fun testBlockStartOnly() {
        doTest(true)
    }

    fun testComplexLangId() {
        doTest(true)
    }

    fun testAutoCommand() {
        doTest(true)
    }

//    fun testCommandAndSymbol() {
//        doTest(true)
//    }

    fun testBrowseWeb() {
        doTest(true)
    }

    fun testAutoRefactor() {
        doTest(true)
    }
}