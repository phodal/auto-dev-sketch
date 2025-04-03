package cc.unitmesh.sketch.llms.recording

class EmptyRecording: Recording {
    override fun write(instruction: RecordingInstruction) {
        // do nothing
    }
}