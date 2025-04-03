package cc.unitmesh.sketch.llms.recording

import kotlinx.serialization.Serializable

@Serializable
data class RecordingInstruction(
    val instruction: String,
    val output: String,
)