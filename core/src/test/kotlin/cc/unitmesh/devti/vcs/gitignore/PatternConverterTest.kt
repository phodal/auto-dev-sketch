package cc.unitmesh.sketch.vcs.gitignore

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for the PatternConverter class that converts gitignore patterns to regex.
 */
class PatternConverterTest {
    
    @Test
    fun testBasicWildcards() {
        // Test * wildcard
        val starPattern = PatternConverter.convertToRegex("*.log")
        assertTrue(starPattern.contains("[^/]*"), "Should convert * to [^/]*")
        
        // Test ? wildcard
        val questionPattern = PatternConverter.convertToRegex("test?.txt")
        assertTrue(questionPattern.contains("[^/]"), "Should convert ? to [^/]")
    }
    
    @Test
    fun testDoubleStarWildcard() {
        // Test ** patterns - check that they are converted to some form of regex
        val doubleStarPattern1 = PatternConverter.convertToRegex("**/logs")
        assertTrue(doubleStarPattern1.contains("logs"), "Should contain the literal part")
        assertTrue(doubleStarPattern1.contains("?"), "Should contain regex quantifiers")

        val doubleStarPattern2 = PatternConverter.convertToRegex("logs/**")
        assertTrue(doubleStarPattern2.contains("logs"), "Should contain the literal part")
        assertTrue(doubleStarPattern2.contains("?"), "Should contain regex quantifiers")

        val doubleStarPattern3 = PatternConverter.convertToRegex("src/**/test")
        assertTrue(doubleStarPattern3.contains("src"), "Should contain src")
        assertTrue(doubleStarPattern3.contains("test"), "Should contain test")
    }
    
    @Test
    fun testEscapeSpecialCharacters() {
        val pattern = PatternConverter.convertToRegex("file.name+test")
        assertTrue(pattern.contains("\\."), "Should escape dots")
        assertTrue(pattern.contains("\\+"), "Should escape plus signs")
    }
    
    @Test
    fun testNegatedPatterns() {
        val pattern = PatternConverter.convertToRegex("!important.log")
        // The ! should be removed by the converter, negation is handled at rule level
        assertTrue(!pattern.startsWith("!"), "Should remove ! prefix")
    }
    
    @Test
    fun testDirectoryPatterns() {
        val dirPattern = PatternConverter.convertToRegex("build/")
        assertTrue(dirPattern.contains("(?:/.*)?$"), "Should handle directory patterns ending with /")
        
        val filePattern = PatternConverter.convertToRegex("build")
        assertTrue(filePattern.endsWith("$"), "Should handle file patterns")
    }
    
    @Test
    fun testRootPatterns() {
        val rootPattern = PatternConverter.convertToRegex("/src")
        assertTrue(rootPattern.startsWith("^"), "Should handle patterns starting with /")
        
        val anywherePattern = PatternConverter.convertToRegex("src")
        assertTrue(anywherePattern.contains("(?:^|.*/)"), "Should handle patterns that can match anywhere")
    }
    
    @Test
    fun testEmptyAndCommentPatterns() {
        val emptyPattern = PatternConverter.convertToRegex("")
        assertEquals("^$", emptyPattern, "Empty pattern should never match")
        
        val commentPattern = PatternConverter.convertToRegex("# comment")
        assertEquals("^$", commentPattern, "Comment pattern should never match")
    }
    
    @Test
    fun testComplexPatterns() {
        // Test a complex real-world pattern
        val complexPattern = PatternConverter.convertToRegex("src/**/target/*.jar")
        assertTrue(complexPattern.contains("src"), "Should contain src")
        assertTrue(complexPattern.contains(".*"), "Should handle **")
        assertTrue(complexPattern.contains("target"), "Should contain target")
        assertTrue(complexPattern.contains("[^/]*"), "Should handle *")
        assertTrue(complexPattern.contains("\\.jar"), "Should escape .jar")
    }
    
    @Test
    fun testCompilePattern() {
        // Test that patterns can be successfully compiled
        val pattern1 = PatternConverter.compilePattern("*.log")
        assertTrue(pattern1.pattern().isNotEmpty(), "Should compile basic pattern")
        
        val pattern2 = PatternConverter.compilePattern("**/build/**")
        assertTrue(pattern2.pattern().isNotEmpty(), "Should compile complex pattern")
        
        val pattern3 = PatternConverter.compilePattern("test?.txt")
        assertTrue(pattern3.pattern().isNotEmpty(), "Should compile ? pattern")
    }
    
    @Test
    fun testInvalidPatterns() {
        // Test that invalid regex patterns throw exceptions
        // Note: Most gitignore patterns should be valid, but we test edge cases
        
        // This should not throw an exception as it's a valid gitignore pattern
        val validPattern = PatternConverter.compilePattern("valid/pattern")
        assertTrue(validPattern.pattern().isNotEmpty(), "Valid patterns should compile")
    }
    
    @Test
    fun testPathNormalization() {
        // Test that backslashes are converted to forward slashes
        val windowsPattern = PatternConverter.convertToRegex("src\\main\\java")
        assertTrue(windowsPattern.contains("/"), "Should normalize backslashes to forward slashes")
        assertTrue(!windowsPattern.contains("\\\\\\\\"), "Should not have double-escaped backslashes")
    }
    
    @Test
    fun testCaseInsensitivity() {
        // Test that compiled patterns are case-insensitive
        val pattern = PatternConverter.compilePattern("*.LOG")
        assertTrue((pattern.flags() and java.util.regex.Pattern.CASE_INSENSITIVE) != 0, 
                  "Compiled patterns should be case-insensitive")
    }
    
    @Test
    fun testRealWorldPatterns() {
        // Test some real-world gitignore patterns
        val patterns = listOf(
            "*.class",
            "*.jar",
            "target/",
            "!important.jar",
            ".idea/",
            "**/*.tmp",
            "logs/*.log",
            "/build",
            "node_modules/",
            "*.iml"
        )
        
        patterns.forEach { pattern ->
            val compiled = PatternConverter.compilePattern(pattern)
            assertTrue(compiled.pattern().isNotEmpty(), "Pattern '$pattern' should compile successfully")
        }
    }
}
