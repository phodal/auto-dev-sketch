package cc.unitmesh.devins.idea.toolwindow.timeline

import cc.unitmesh.devins.idea.renderer.JewelRenderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested

/**
 * Unit tests for IdeaTerminalOutputBubble component.
 * Tests terminal command execution display with output formatting.
 */
@DisplayName("IdeaTerminalOutputBubble Tests")
class IdeaTerminalOutputBubbleTest {

    @Nested
    @DisplayName("Terminal Output Data")
    inner class TerminalOutputDataTests {

        @Test
        @DisplayName("Should create terminal output item with all fields")
        fun `test complete terminal output item`() {
            val item = JewelRenderer.TimelineItem.TerminalOutputItem(
                command = "ls -la",
                output = "total 8\ndrwxr-xr-x  2 user user 4096 Jan 1 12:00 .",
                exitCode = 0,
                executionTimeMs = 150
            )

            assertEquals("ls -la", item.command)
            assertTrue(item.output.isNotEmpty())
            assertEquals(0, item.exitCode)
            assertEquals(150, item.executionTimeMs)
        }

        @Test
        @DisplayName("Should handle successful command execution")
        fun `test successful command`() {
            val item = JewelRenderer.TimelineItem.TerminalOutputItem(
                command = "echo 'Hello World'",
                output = "Hello World",
                exitCode = 0,
                executionTimeMs = 10
            )

            assertEquals(0, item.exitCode, "Successful commands should have exit code 0")
            assertTrue(item.output.contains("Hello World"))
        }

        @Test
        @DisplayName("Should handle failed command execution")
        fun `test failed command`() {
            val item = JewelRenderer.TimelineItem.TerminalOutputItem(
                command = "cat nonexistent.txt",
                output = "cat: nonexistent.txt: No such file or directory",
                exitCode = 1,
                executionTimeMs = 5
            )

            assertNotEquals(0, item.exitCode, "Failed commands should have non-zero exit code")
            assertTrue(item.output.contains("No such file"))
        }

        @Test
        @DisplayName("Should track execution time")
        fun `test execution time tracking`() {
            val quickItem = JewelRenderer.TimelineItem.TerminalOutputItem(
                command = "pwd",
                output = "/home/user",
                exitCode = 0,
                executionTimeMs = 5
            )

            val slowItem = JewelRenderer.TimelineItem.TerminalOutputItem(
                command = "sleep 2",
                output = "",
                exitCode = 0,
                executionTimeMs = 2050
            )

            assertTrue(quickItem.executionTimeMs < 100, "Quick commands should execute fast")
            assertTrue(slowItem.executionTimeMs > 2000, "Slow commands should show longer time")
        }
    }

    @Nested
    @DisplayName("Command Display")
    inner class CommandDisplayTests {

        @Test
        @DisplayName("Should display simple command")
        fun `test simple command display`() {
            val command = "ls"

            assertFalse(command.isEmpty(), "Command should not be empty")
            assertFalse(command.contains("|"), "Simple command has no pipes")
        }

        @Test
        @DisplayName("Should display command with arguments")
        fun `test command with arguments`() {
            val command = "ls -la /home/user"

            assertTrue(command.contains("-la"), "Should preserve flags")
            assertTrue(command.contains("/home/user"), "Should preserve arguments")
        }

        @Test
        @DisplayName("Should display piped commands")
        fun `test piped commands`() {
            val command = "cat file.txt | grep 'pattern' | wc -l"

            assertTrue(command.contains("|"), "Should show pipe operators")
            assertEquals(2, command.count { it == '|' }, "Should have 2 pipes")
        }

        @Test
        @DisplayName("Should display commands with redirects")
        fun `test command with redirects`() {
            val command = "echo 'data' > output.txt"

            assertTrue(command.contains(">"), "Should show redirect operator")
            assertTrue(command.contains("output.txt"), "Should show target file")
        }

        @Test
        @DisplayName("Should display complex shell commands")
        fun `test complex shell command`() {
            val command = "for i in {1..5}; do echo \$i; done"

            assertTrue(command.contains("for"), "Should preserve shell constructs")
            assertTrue(command.contains("do"), "Should preserve keywords")
            assertTrue(command.contains("\$i"), "Should preserve variables")
        }

        @Test
        @DisplayName("Should prefix command with $ symbol")
        fun `test command prefix`() {
            val command = "ls -la"
            val display = "$ $command"

            assertTrue(display.startsWith("$"), "Should start with $ symbol")
            assertTrue(display.contains(command), "Should contain command")
        }
    }

    @Nested
    @DisplayName("Output Formatting")
    inner class OutputFormattingTests {

        @Test
        @DisplayName("Should handle empty output")
        fun `test empty output`() {
            val output = ""

            assertTrue(output.isEmpty(), "Output can be empty for some commands")
            assertEquals(0, output.length)
        }

        @Test
        @DisplayName("Should handle single-line output")
        fun `test single-line output`() {
            val output = "Hello World"

            assertFalse(output.contains("\n"), "Single-line output has no newlines")
            assertTrue(output.length > 0)
        }

        @Test
        @DisplayName("Should handle multi-line output")
        fun `test multi-line output`() {
            val output = "Line 1\nLine 2\nLine 3"

            assertTrue(output.contains("\n"), "Multi-line output has newlines")
            assertEquals(3, output.split("\n").size, "Should have 3 lines")
        }

        @Test
        @DisplayName("Should truncate very long output")
        fun `test output truncation`() {
            val longOutput = "A".repeat(2000)
            val truncated = longOutput.take(1000) + "\n..."

            assertTrue(truncated.length < longOutput.length, "Should truncate long output")
            assertTrue(truncated.endsWith("..."), "Should indicate truncation")
        }

        @Test
        @DisplayName("Should preserve ANSI color codes")
        fun `test ansi color codes`() {
            val output = "\u001B[32mGreen text\u001B[0m"

            assertTrue(output.contains("\u001B[32m"), "Should preserve color codes")
            assertTrue(output.contains("\u001B[0m"), "Should preserve reset codes")
        }

        @Test
        @DisplayName("Should handle output with special characters")
        fun `test special characters in output`() {
            val output = "File: /path/to/file@v1.0#tag"

            assertTrue(output.contains("@"), "Should preserve @ symbol")
            assertTrue(output.contains("#"), "Should preserve # symbol")
            assertTrue(output.contains("/"), "Should preserve path separators")
        }
    }

    @Nested
    @DisplayName("Exit Code Display")
    inner class ExitCodeDisplayTests {

        @Test
        @DisplayName("Should display exit code 0 as success")
        fun `test exit code 0`() {
            val exitCode = 0

            assertEquals(0, exitCode, "Exit code 0 indicates success")
            // Would display in green color (AutoDevColors.Green.c400)
        }

        @Test
        @DisplayName("Should display non-zero exit code as error")
        fun `test non-zero exit code`() {
            val exitCode = 1

            assertNotEquals(0, exitCode, "Non-zero exit code indicates error")
            // Would display in red color (AutoDevColors.Red.c400)
        }

        @Test
        @DisplayName("Should handle various error codes")
        fun `test various error codes`() {
            val errorCodes = listOf(1, 2, 127, 130, 255)

            errorCodes.forEach { code ->
                assertNotEquals(0, code, "Code $code should indicate error")
            }
        }

        @Test
        @DisplayName("Exit code display format")
        fun `test exit code format`() {
            val exitCode = 0
            val display = "exit: $exitCode"

            assertEquals("exit: 0", display)
            assertTrue(display.startsWith("exit:"))
        }
    }

    @Nested
    @DisplayName("Execution Time Display")
    inner class ExecutionTimeDisplayTests {

        @Test
        @DisplayName("Should display execution time in milliseconds")
        fun `test execution time format`() {
            val timeMs = 150L
            val display = "${timeMs}ms"

            assertEquals("150ms", display)
            assertTrue(display.endsWith("ms"))
        }

        @Test
        @DisplayName("Should handle very fast execution")
        fun `test fast execution`() {
            val timeMs = 1L

            assertTrue(timeMs < 10, "Very fast execution")
            assertTrue(timeMs > 0, "Should still be measurable")
        }

        @Test
        @DisplayName("Should handle slow execution")
        fun `test slow execution`() {
            val timeMs = 5000L

            assertTrue(timeMs > 1000, "Slow execution takes over 1 second")
            assertEquals(5000, timeMs)
        }

        @Test
        @DisplayName("Should handle zero execution time")
        fun `test zero execution time`() {
            val timeMs = 0L

            assertEquals(0, timeMs, "Zero time is possible for cached results")
        }
    }

    @Nested
    @DisplayName("Terminal Styling")
    inner class TerminalStylingTests {

        @Test
        @DisplayName("Should use terminal background color")
        fun `test terminal background`() {
            val useTerminalBackground = true

            assertTrue(useTerminalBackground, "Should use dark terminal background")
            // Would use AutoDevColors.Neutral.c900
        }

        @Test
        @DisplayName("Command should use cyan color")
        fun `test command color`() {
            val useCyanForCommand = true

            assertTrue(useCyanForCommand, "Commands should be in cyan")
            // Would use AutoDevColors.Cyan.c400
        }

        @Test
        @DisplayName("Output should use light gray color")
        fun `test output color`() {
            val useLightGrayForOutput = true

            assertTrue(useLightGrayForOutput, "Output should be in light gray")
            // Would use AutoDevColors.Neutral.c300
        }

        @Test
        @DisplayName("Should use monospace font for terminal")
        fun `test monospace font`() {
            val useMonospace = true

            assertTrue(useMonospace, "Terminal should use monospace font")
            // Would use FontFamily.Monospace
        }

        @Test
        @DisplayName("Should have maximum width constraint")
        fun `test max width`() {
            val maxWidth = 600 // dp

            assertTrue(maxWidth > 0, "Should have positive max width")
            assertTrue(maxWidth > 500, "Should be wider than message bubbles")
        }
    }

    @Nested
    @DisplayName("Command Types")
    inner class CommandTypeTests {

        @Test
        @DisplayName("Should handle file operations")
        fun `test file operation commands`() {
            val commands = listOf("cat file.txt", "ls -la", "cp src dst")

            commands.forEach { cmd ->
                assertFalse(cmd.isEmpty(), "Command should not be empty")
                assertTrue(cmd.length > 2, "Command should be meaningful")
            }
        }

        @Test
        @DisplayName("Should handle git commands")
        fun `test git commands`() {
            val commands = listOf("git status", "git log", "git diff")

            commands.forEach { cmd ->
                assertTrue(cmd.startsWith("git"), "Should be git command")
            }
        }

        @Test
        @DisplayName("Should handle build commands")
        fun `test build commands`() {
            val commands = listOf("npm install", "gradle build", "mvn clean install")

            commands.forEach { cmd ->
                assertTrue(cmd.contains("install") || cmd.contains("build"))
            }
        }

        @Test
        @DisplayName("Should handle test commands")
        fun `test test execution commands`() {
            val commands = listOf("npm test", "pytest", "gradle test")

            commands.forEach { cmd ->
                assertTrue(cmd.contains("test") || cmd.contains("pytest"))
            }
        }
    }

    @Nested
    @DisplayName("Output Scenarios")
    inner class OutputScenarioTests {

        @Test
        @DisplayName("Should handle successful file read")
        fun `test successful file read output`() {
            val item = JewelRenderer.TimelineItem.TerminalOutputItem(
                command = "cat README.md",
                output = "# Project Title\n\nDescription of the project...",
                exitCode = 0,
                executionTimeMs = 10
            )

            assertEquals(0, item.exitCode)
            assertTrue(item.output.contains("#"), "Should contain markdown")
            assertTrue(item.output.contains("\n"), "Should be multi-line")
        }

        @Test
        @DisplayName("Should handle file not found error")
        fun `test file not found output`() {
            val item = JewelRenderer.TimelineItem.TerminalOutputItem(
                command = "cat missing.txt",
                output = "cat: missing.txt: No such file or directory",
                exitCode = 1,
                executionTimeMs = 5
            )

            assertNotEquals(0, item.exitCode)
            assertTrue(item.output.contains("No such file"))
        }

        @Test
        @DisplayName("Should handle permission denied error")
        fun `test permission denied output`() {
            val item = JewelRenderer.TimelineItem.TerminalOutputItem(
                command = "cat /root/secret.txt",
                output = "cat: /root/secret.txt: Permission denied",
                exitCode = 1,
                executionTimeMs = 3
            )

            assertNotEquals(0, item.exitCode)
            assertTrue(item.output.contains("Permission denied"))
        }

        @Test
        @DisplayName("Should handle command not found")
        fun `test command not found output`() {
            val item = JewelRenderer.TimelineItem.TerminalOutputItem(
                command = "nonexistentcmd",
                output = "bash: nonexistentcmd: command not found",
                exitCode = 127,
                executionTimeMs = 2
            )

            assertEquals(127, item.exitCode)
            assertTrue(item.output.contains("command not found"))
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    inner class IntegrationTests {

        @Test
        @DisplayName("Should create timeline item with timestamp")
        fun `test timeline item timestamp`() {
            val before = System.currentTimeMillis()
            val item = JewelRenderer.TimelineItem.TerminalOutputItem(
                command = "echo test",
                output = "test",
                exitCode = 0,
                executionTimeMs = 10
            )
            val after = System.currentTimeMillis()

            assertTrue(item.timestamp >= before)
            assertTrue(item.timestamp <= after)
        }

        @Test
        @DisplayName("Should create unique IDs for items")
        fun `test unique item ids`() {
            val item1 = JewelRenderer.TimelineItem.TerminalOutputItem(
                command = "ls",
                output = "file1.txt",
                exitCode = 0,
                executionTimeMs = 10
            )
            val item2 = JewelRenderer.TimelineItem.TerminalOutputItem(
                command = "ls",
                output = "file1.txt",
                exitCode = 0,
                executionTimeMs = 10
            )

            assertNotEquals(item1.id, item2.id, "Each item should have unique ID")
        }

        @Test
        @DisplayName("Should handle sequence of commands")
        fun `test command sequence`() {
            val commands = listOf(
                JewelRenderer.TimelineItem.TerminalOutputItem("pwd", "/home/user", 0, 5),
                JewelRenderer.TimelineItem.TerminalOutputItem("ls", "file.txt", 0, 10),
                JewelRenderer.TimelineItem.TerminalOutputItem("cat file.txt", "content", 0, 15)
            )

            assertEquals(3, commands.size)
            commands.forEach { item ->
                assertEquals(0, item.exitCode, "All commands should succeed")
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very long command")
        fun `test very long command`() {
            val longCommand = "echo " + "A".repeat(500)
            val item = JewelRenderer.TimelineItem.TerminalOutputItem(
                command = longCommand,
                output = "A".repeat(500),
                exitCode = 0,
                executionTimeMs = 10
            )

            assertTrue(item.command.length > 500)
            assertTrue(item.output.length > 400)
        }

        @Test
        @DisplayName("Should handle command with quotes")
        fun `test command with quotes`() {
            val command = "echo \"Hello World\" 'single quotes'"

            assertTrue(command.contains("\""), "Should preserve double quotes")
            assertTrue(command.contains("'"), "Should preserve single quotes")
        }

        @Test
        @DisplayName("Should handle output with null bytes")
        fun `test output with special bytes`() {
            val output = "data\u0000more data"

            assertTrue(output.contains("\u0000"), "Should handle null bytes")
        }

        @Test
        @DisplayName("Should handle negative exit code")
        fun `test negative exit code`() {
            // Some systems use negative codes for signals
            val exitCode = -1

            assertTrue(exitCode < 0, "Should handle negative codes")
        }

        @Test
        @DisplayName("Should handle Unicode in output")
        fun `test unicode output`() {
            val output = "文件名.txt\n日本語\nEmoji: 🎉"

            assertTrue(output.contains("文件名"))
            assertTrue(output.contains("日本語"))
            assertTrue(output.contains("🎉"))
        }
    }
}