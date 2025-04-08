package cc.unitmesh.sketch.provider.context

import kotlin.reflect.KClass

class ChatContextItem(
    val clazz: KClass<*>,
    var text: String
)