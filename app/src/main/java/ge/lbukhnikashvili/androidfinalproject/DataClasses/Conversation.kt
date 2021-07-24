package ge.lbukhnikashvili.androidfinalproject.DataClasses

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Conversation(
    var targetuid: String? = "",
    var messages: MutableList<Message> =mutableListOf()
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "targetuid" to targetuid,
            "messages" to messages,
        )
    }
}