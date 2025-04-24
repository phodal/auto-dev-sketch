package cc.unitmesh.sketch.language.compiler.execute

import cc.unitmesh.sketch.language.processor.CrawlProcessor
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CrawlProcessorTest : BasePlatformTestCase() {
    fun testShouldParseLink() {
        val urls = arrayOf("https://ide.unitmesh.cc/local-agent")
        val results = CrawlProcessor.execute(urls)
        assertEquals(results.size, 1)
    }
}
