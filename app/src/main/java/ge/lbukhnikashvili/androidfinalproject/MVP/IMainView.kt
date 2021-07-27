package ge.lbukhnikashvili.androidfinalproject.MVP
import android.graphics.Bitmap
import ge.lbukhnikashvili.androidfinalproject.DataClasses.*
import ge.lbukhnikashvili.androidfinalproject.MainActivity

interface IMainView {
    fun usersBriefInfoReceived(data: MutableList<UserInfo>)
    fun userConversationUpdated(successfully: Boolean, conversation: Conversation?)

    fun prepareForOpenMainPage()
    fun showLayout(page: MainActivity.PageType)
    fun showToast(message: String)
    fun mainPageDataUpdated(conversationsInfo: MutableList<ConversationInfo>)
}