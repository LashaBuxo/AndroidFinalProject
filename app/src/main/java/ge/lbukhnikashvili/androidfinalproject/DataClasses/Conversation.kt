package ge.lbukhnikashvili.androidfinalproject.DataClasses

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Conversation(
    var messages: MutableMap<String,Message> = HashMap()
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "messages" to messages,
        )
    }
}