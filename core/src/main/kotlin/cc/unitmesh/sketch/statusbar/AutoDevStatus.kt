package cc.unitmesh.sketch.statusbar

import cc.unitmesh.sketch.AutoDevIcons
import javax.swing.Icon

enum class AutoDevStatus {
    WAITING,
    Ready,
    InProgress,
    Error,
    Done;

    val icon: Icon
        get() {
            return when (this) {
                WAITING -> AutoDevIcons.DARK
                Ready -> AutoDevIcons.AI_COPILOT
                InProgress -> AutoDevIcons.LOADING
                Error -> AutoDevIcons.AUTODEV_ERROR
                Done -> AutoDevIcons.AI_COPILOT
            }
        }
}
