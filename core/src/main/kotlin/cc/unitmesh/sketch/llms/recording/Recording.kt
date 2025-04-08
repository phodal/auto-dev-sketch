package cc.unitmesh.sketch.llms.recording

interface Recording {
    fun write(instruction: RecordingInstruction)
}
