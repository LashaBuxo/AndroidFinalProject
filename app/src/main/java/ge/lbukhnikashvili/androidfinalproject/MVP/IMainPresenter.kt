package ge.lbukhnikashvili.androidfinalproject.MVP

import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.database.DataSnapshot
import ge.lbukhnikashvili.androidfinalproject.DataClasses.User
import ge.lbukhnikashvili.androidfinalproject.DataClasses.UserInfo

interface IMainPresenter {
    fun onUserStartedApp(isLoggedIn: Boolean)
    fun onUserSigned(successfully: Boolean, failReason: String, isRegistering: Boolean)

    fun retrievedUsersBriefInfo(usersBriefInfo: MutableList<UserInfo>)
    fun userParametersUpdated(successfully:Boolean)
}