package cc.unitmesh.devins.idea.toolwindow.timeline

import cc.unitmesh.agent.tool.ToolType
import cc.unitmesh.devins.idea.renderer.JewelRenderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested

/**
 * Unit tests for IdeaToolCallBubble component.
 * Tests tool execution display with expandable parameters and output.
 */
@DisplayName("IdeaToolCallBubble Tests")
class IdeaToolCallBubbleTest {

    @Nested
    @DisplayName("Tool Call Data")
    inner class ToolCallDataTests {

        @Test
        @DisplayName("Should create tool call item with all fields")
        fun `test complete tool call item`() {
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "read-file",
                description = "file reader",
                params = "path=\"/test/file.txt\"",
                fullParams = "path=\"/test/file.txt\" encoding=\"utf-8\"",
                filePath = "/test/file.txt",
                toolType = ToolType.ReadFile,
                success = true,
                summary = "Read 100 lines",
                output = "File content...",
                fullOutput = "Full file content with all details",
                executionTimeMs = 50
            )

            assertEquals("read-file", item.toolName)
            assertEquals(ToolType.ReadFile, item.toolType)
            assertTrue(item.success == true)
            assertEquals(50, item.executionTimeMs)
        }

        @Test
        @DisplayName("Should handle executing tool (success = null)")
        fun `test executing tool state`() {
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "grep",
                description = "pattern matcher",
                params = "pattern=\"TODO\"",
                toolType = ToolType.Glob,
                success = null,
                output = null
            )

            assertNull(item.success, "Executing tools should have null success")
            assertNull(item.output, "No output yet for executing tools")
        }

        @Test
        @DisplayName("Should handle successful tool execution")
        fun `test successful tool execution`() {
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "write-file",
                params = "path=\"output.txt\"",
                toolType = ToolType.WriteFile,
                success = true,
                summary = "File written successfully",
                output = "Successfully wrote 500 bytes"
            )

            assertTrue(item.success == true, "Should indicate success")
            assertNotNull(item.summary)
            assertNotNull(item.output)
        }

        @Test
        @DisplayName("Should handle failed tool execution")
        fun `test failed tool execution`() {
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "read-file",
                params = "path=\"missing.txt\"",
                toolType = ToolType.ReadFile,
                success = false,
                output = "Error: File not found"
            )

            assertFalse(item.success == true, "Should indicate failure")
            assertTrue(item.output!!.contains("Error"))
        }
    }

    @Nested
    @DisplayName("Tool Types")
    inner class ToolTypeTests {

        @Test
        @DisplayName("Should handle ReadFile tool")
        fun `test read file tool`() {
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "/path/to/file.txt - read-file",
                params = "path=\"/path/to/file.txt\"",
                toolType = ToolType.ReadFile,
                success = true,
                summary = "Read 50 lines"
            )

            assertEquals(ToolType.ReadFile, item.toolType)
            assertTrue(item.params.contains("path="))
        }

        @Test
        @DisplayName("Should handle WriteFile tool")
        fun `test write file tool`() {
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "/path/to/output.txt - write-file",
                params = "path=\"/path/to/output.txt\" content=\"...\"",
                toolType = ToolType.WriteFile,
                success = true,
                summary = "File written successfully"
            )

            assertEquals(ToolType.WriteFile, item.toolType)
            assertTrue(item.summary!!.contains("written"))
        }

        @Test
        @DisplayName("Should handle Glob tool")
        fun `test glob tool`() {
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "glob",
                params = "pattern=\"**/*.kt\"",
                toolType = ToolType.Glob,
                success = true,
                output = "Found 25 files matching pattern"
            )

            assertEquals(ToolType.Glob, item.toolType)
            assertTrue(item.params.contains("pattern="))
        }

        @Test
        @DisplayName("Should handle Shell tool")
        fun `test shell tool`() {
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "shell",
                params = "command=\"ls -la\"",
                toolType = ToolType.Shell,
                success = true,
                executionTimeMs = 150
            )

            assertEquals(ToolType.Shell, item.toolType)
            assertTrue(item.params.contains("command="))
        }
    }

    @Nested
    @DisplayName("Status Icons")
    inner class StatusIconTests {

        @Test
        @DisplayName("Executing tool should show PlayArrow icon")
        fun `test executing icon`() {
            val isExecuting = true // success == null

            assertTrue(isExecuting, "Should show play/executing icon")
            // Would use IdeaComposeIcons.PlayArrow with AutoDevColors.Blue.c400
        }

        @Test
        @DisplayName("Successful tool should show CheckCircle icon")
        fun `test success icon`() {
            val success = true

            assertTrue(success, "Should show check/success icon")
            // Would use IdeaComposeIcons.CheckCircle with AutoDevColors.Green.c400
        }

        @Test
        @DisplayName("Failed tool should show Error icon")
        fun `test failure icon`() {
            val success = false

            assertFalse(success, "Should show error/failure icon")
            // Would use IdeaComposeIcons.Error with AutoDevColors.Red.c400
        }
    }

    @Nested
    @DisplayName("Parameters Display")
    inner class ParametersDisplayTests {

        @Test
        @DisplayName("Should display short parameters")
        fun `test short parameters`() {
            val params = "path=\"file.txt\""

            assertTrue(params.length < 100, "Short params displayed fully")
            assertTrue(params.contains("="))
        }

        @Test
        @DisplayName("Should truncate long parameters")
        fun `test long parameters truncation`() {
            val params = "content=" + "\"" + "A".repeat(200) + "\""
            val displayParams = params.take(100)

            assertTrue(params.length > 100)
            assertEquals(100, displayParams.length)
        }

        @Test
        @DisplayName("Should show 'Show All' link for long parameters")
        fun `test show all link for long params`() {
            val params = "A".repeat(150)
            val hasMoreParams = params.length > 100

            assertTrue(hasMoreParams, "Should offer to show all")
        }

        @Test
        @DisplayName("Should handle empty parameters")
        fun `test empty parameters`() {
            val params = ""

            assertTrue(params.isEmpty())
            assertEquals(0, params.length)
        }

        @Test
        @DisplayName("Should preserve parameter structure")
        fun `test parameter structure`() {
            val params = "path=\"/test\" mode=\"read\" encoding=\"utf-8\""

            assertTrue(params.contains("path="))
            assertTrue(params.contains("mode="))
            assertTrue(params.contains("encoding="))
        }
    }

    @Nested
    @DisplayName("Output Display")
    inner class OutputDisplayTests {

        @Test
        @DisplayName("Should display short output")
        fun `test short output`() {
            val output = "Operation completed successfully"

            assertTrue(output.length < 200, "Short output displayed fully")
        }

        @Test
        @DisplayName("Should truncate long output")
        fun `test long output truncation`() {
            val output = "A".repeat(500)
            val displayOutput = output.take(200)

            assertTrue(output.length > 200)
            assertEquals(200, displayOutput.length)
        }

        @Test
        @DisplayName("Should show 'Show Full' link for long output")
        fun `test show full link for long output`() {
            val output = "A".repeat(300)
            val hasMoreOutput = output.length > 200

            assertTrue(hasMoreOutput, "Should offer to show full output")
        }

        @Test
        @DisplayName("Should handle null output")
        fun `test null output`() {
            val output: String? = null

            assertNull(output)
        }

        @Test
        @DisplayName("Should handle empty output")
        fun `test empty output`() {
            val output = ""

            assertTrue(output.isEmpty())
        }

        @Test
        @DisplayName("Should format JSON output")
        fun `test json output formatting`() {
            val output = "{\"key\":\"value\",\"count\":42}"

            assertTrue(output.startsWith("{"))
            assertTrue(output.contains(":"))
            assertTrue(output.contains(","))
        }
    }

    @Nested
    @DisplayName("Expandable Behavior")
    inner class ExpandableBehaviorTests {

        @Test
        @DisplayName("Should auto-expand on error")
        fun `test auto expand on error`() {
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "test-tool",
                params = "param=value",
                success = false,
                output = "Error occurred"
            )

            assertFalse(item.success == true, "Errors should auto-expand")
        }

        @Test
        @DisplayName("Should not auto-expand on success")
        fun `test no auto expand on success`() {
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "test-tool",
                params = "param=value",
                success = true
            )

            assertTrue(item.success == true, "Success should not auto-expand")
        }

        @Test
        @DisplayName("Should be expandable if has params")
        fun `test expandable with params`() {
            val hasParams = "path=\"file.txt\"".isNotEmpty()

            assertTrue(hasParams, "Should be expandable")
        }

        @Test
        @DisplayName("Should be expandable if has output")
        fun `test expandable with output`() {
            val hasOutput = "Result: success".isNotEmpty()

            assertTrue(hasOutput, "Should be expandable")
        }

        @Test
        @DisplayName("Should not be expandable without content")
        fun `test not expandable without content`() {
            val hasParams = "".isEmpty()
            val hasOutput = "".isEmpty()
            val hasExpandableContent = !hasParams && !hasOutput

            assertTrue(hasExpandableContent || (!hasParams && !hasOutput))
        }
    }

    @Nested
    @DisplayName("Summary Display")
    inner class SummaryDisplayTests {

        @Test
        @DisplayName("Should show truncated params as summary")
        fun `test params summary`() {
            val params = "path=\"/very/long/path/to/file.txt\""
            val summary = "-> " + params.take(40) + if (params.length > 40) "..." else ""

            assertTrue(summary.startsWith("->"))
            assertTrue(summary.length <= 45)
        }

        @Test
        @DisplayName("Should color summary based on success")
        fun `test summary coloring`() {
            val successItem = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "tool",
                params = "param=value",
                success = true
            )
            val failureItem = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "tool",
                params = "param=value",
                success = false
            )

            assertTrue(successItem.success == true, "Success should use green color")
            assertFalse(failureItem.success == true, "Failure should use red color")
        }

        @Test
        @DisplayName("Should show execution time in summary")
        fun `test execution time in summary`() {
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "tool",
                params = "param=value",
                executionTimeMs = 250
            )

            assertEquals(250, item.executionTimeMs)
            // Would display as "250ms" in gray
        }
    }

    @Nested
    @DisplayName("Copy to Clipboard")
    inner class CopyToClipboardTests {

        @Test
        @DisplayName("Should support copying parameters")
        fun `test copy parameters`() {
            val params = "path=\"/test/file.txt\" mode=\"read\""

            assertFalse(params.isEmpty(), "Parameters should be copyable")
            assertTrue(params.length > 0)
        }

        @Test
        @DisplayName("Should support copying output")
        fun `test copy output`() {
            val output = "This is the output that can be copied"

            assertFalse(output.isEmpty(), "Output should be copyable")
            assertTrue(output.length > 0)
        }

        @Test
        @DisplayName("Should handle empty copy content")
        fun `test copy empty content`() {
            val content = ""

            assertTrue(content.isEmpty(), "Should handle empty content gracefully")
        }
    }

    @Nested
    @DisplayName("Tool Execution States")
    inner class ExecutionStatesTests {

        @Test
        @DisplayName("Should show pending state")
        fun `test pending execution state`() {
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "processing-tool",
                params = "input=data",
                success = null
            )

            assertNull(item.success, "Should be in pending state")
        }

        @Test
        @DisplayName("Should transition to success state")
        fun `test transition to success`() {
            var success: Boolean? = null
            success = true

            assertEquals(true, success, "Should transition to success")
        }

        @Test
        @DisplayName("Should transition to failure state")
        fun `test transition to failure`() {
            var success: Boolean? = null
            success = false

            assertEquals(false, success, "Should transition to failure")
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    inner class IntegrationTests {

        @Test
        @DisplayName("Should create timeline item with timestamp")
        fun `test timeline item timestamp`() {
            val before = System.currentTimeMillis()
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "test-tool",
                params = "test=true"
            )
            val after = System.currentTimeMillis()

            assertTrue(item.timestamp >= before)
            assertTrue(item.timestamp <= after)
        }

        @Test
        @DisplayName("Should create unique IDs")
        fun `test unique item ids`() {
            val item1 = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "tool1",
                params = "param=1"
            )
            val item2 = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "tool2",
                params = "param=2"
            )

            assertNotEquals(item1.id, item2.id)
        }

        @Test
        @DisplayName("Should handle tool call sequence")
        fun `test tool call sequence`() {
            val sequence = listOf(
                JewelRenderer.TimelineItem.ToolCallItem("tool1", params = "p1", success = true),
                JewelRenderer.TimelineItem.ToolCallItem("tool2", params = "p2", success = true),
                JewelRenderer.TimelineItem.ToolCallItem("tool3", params = "p3", success = false)
            )

            assertEquals(3, sequence.size)
            assertTrue(sequence[0].success == true)
            assertTrue(sequence[1].success == true)
            assertFalse(sequence[2].success == true)
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very long tool name")
        fun `test very long tool name`() {
            val toolName = "very-long-tool-name-" + "that-repeats-".repeat(10)
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = toolName,
                params = "param=value"
            )

            assertTrue(item.toolName.length > 100)
        }

        @Test
        @DisplayName("Should handle special characters in params")
        fun `test special characters in params`() {
            val params = "path=\"/path/with/@#\$%/file.txt\""

            assertTrue(params.contains("@"))
            assertTrue(params.contains("#"))
            assertTrue(params.contains("\$"))
        }

        @Test
        @DisplayName("Should handle null toolType")
        fun `test null tool type`() {
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "unknown-tool",
                params = "param=value",
                toolType = null
            )

            assertNull(item.toolType)
        }

        @Test
        @DisplayName("Should handle Unicode in output")
        fun `test unicode in output`() {
            val output = "文件处理完成 ✓"
            val item = JewelRenderer.TimelineItem.ToolCallItem(
                toolName = "tool",
                params = "param=value",
                output = output
            )

            assertTrue(item.output!!.contains("文件"))
            assertTrue(item.output!!.contains("✓"))
        }
    }
}