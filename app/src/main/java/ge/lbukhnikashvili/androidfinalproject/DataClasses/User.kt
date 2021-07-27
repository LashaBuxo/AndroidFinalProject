package ge.lbukhnikashvili.androidfinalproject.DataClasses

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

//users
@IgnoreExtraProperties
data class User(
    var uid: String? = "",
    var info: UserInfo? =UserInfo("user","default",""),
    var conversations: MutableMap<String,Conversation> = HashMap()
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "info" to info,
            "conversations" to conversations,
        )
    }
}