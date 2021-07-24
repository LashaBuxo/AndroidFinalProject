package ge.lbukhnikashvili.androidfinalproject.DataClasses

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Message(
    var uid: String? = "",
    var message: String? = "",
    var date: String? ="",
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "message" to message,
            "date" to date,
        )
    }
}