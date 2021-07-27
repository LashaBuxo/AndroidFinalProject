package ge.lbukhnikashvili.androidfinalproject.MVP

import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.database.DataSnapshot
import ge.lbukhnikashvili.androidfinalproject.DataClasses.*

interface IMainPresenter {
    fun onUserStartedApp(isLoggedIn: Boolean)
    fun onUserSigned(successfully: Boolean, failReason: String, isRegistering: Boolean)

    fun getUserBriefInfo(uid: String): UserInfo?
    fun retrievedUsersBriefInfo(usersBriefInfo: MutableList<UserInfo>)
    fun userParametersUpdated(successfully:Boolean)
    fun userConversationUpdated(successfully: Boolean, conversation: Conversation?)
    fun mainPageDataUpdated(conversationsInfo: MutableList<ConversationInfo>)
}