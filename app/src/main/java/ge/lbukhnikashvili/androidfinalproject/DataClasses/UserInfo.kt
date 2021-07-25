package ge.lbukhnikashvili.androidfinalproject.DataClasses

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
//userinfos
@IgnoreExtraProperties
data class UserInfo(
    var uid: String? ="",
    var name: String? = "",
    var profession: String? = "",
    var image_url: String? = "",
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "profession" to profession,
            "image_url" to image_url,
        )
    }
}