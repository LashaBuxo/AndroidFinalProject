package ge.lbukhnikashvili.androidfinalproject.MVP
import android.graphics.Bitmap
import ge.lbukhnikashvili.androidfinalproject.DataClasses.User
import ge.lbukhnikashvili.androidfinalproject.DataClasses.UserInfo
import ge.lbukhnikashvili.androidfinalproject.MainActivity

interface IMainView {
    fun usersBriefInfoReceived(data: MutableList<UserInfo>)
    fun showLayout(page: MainActivity.PageType)
    fun showToast(message: String)
}