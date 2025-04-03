package cc.unitmesh.sketch.language.compiler.exec

import cc.unitmesh.sketch.command.InsCommand
import cc.unitmesh.sketch.command.dataprovider.BuiltinCommand

class PrintInsCommand(private val value: String) : InsCommand {
    override val commandName: BuiltinCommand = BuiltinCommand.OPEN

    override suspend fun execute(): String {
        return value
    }
}