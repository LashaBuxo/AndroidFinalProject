package ge.lbukhnikashvili.androidfinalproject.MVP

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.ktx.Firebase
import ge.lbukhnikashvili.androidfinalproject.DataClasses.User
import ge.lbukhnikashvili.androidfinalproject.DataClasses.UserInfo
import ge.lbukhnikashvili.androidfinalproject.MainActivity
import ge.lbukhnikashvili.androidfinalproject.MainActivity.PageType
import ge.lbukhnikashvili.androidfinalproject.R

class MainPresenter(var view: IMainView?) : IMainPresenter {

    private val interactor = MainInteractor(this)
    private lateinit var activity: MainActivity

    //region Commands
    fun initPresenter(application: Application, activity: MainActivity) {
        this.activity=activity
        interactor.initFirebaseForApplication(application, activity)
    }

    fun loginUser(username: String, password: String) {
        if (username.isNullOrBlank() or password.isNullOrBlank()) {
            onUserSigned(false, activity.getString(R.string.fail_unexpected), false)
            return
        }
        view?.showLayout(PageType.Loading)
        interactor.loginUser(username, password)
    }

    fun registerUser(username: String, password: String, profession: String) {
        if (username.isNullOrBlank() or password.isNullOrBlank() or profession.isNullOrBlank()) {
            onUserSigned(false, activity.getString(R.string.fail_blank_lines), true);
            return
        }
        view?.showLayout(PageType.Loading)
        interactor.registerUser(username, password, profession)
    }

    fun logoutUser() {
        interactor.logoutUser()
        view?.showLayout(PageType.Enter)
    }

    fun getCurrentUserData(): User? {
        if (interactor.currentUserData == null) {
            view?.showToast(activity.getString(R.string.fail_unexpected))
            logoutUser()
        }
        return interactor.currentUserData
    }

    fun getUserBriefInfo(uid: String): UserInfo? {
        if (interactor.usersBriefInfo == null) {
            view?.showToast(activity.getString(R.string.fail_unexpected))
            //logoutUser()
            return null
        }
        for (info in interactor.usersBriefInfo) {
            if (info.uid == uid) return info
        }

        //User with such uid didn't find
        view?.showToast(activity.getString(R.string.fail_unexpected))
        return null
    }


    fun updateUserParameters(newName: String, newProfession: String, uploadedImageUri: Uri?) {
        interactor.updateUserParameters(newName, newProfession, uploadedImageUri)
    }

    fun requestUsersBriefInfo() {
        interactor.requestUsersBriefInfo()
    }
    //endregion

    //region Callbacks
    override fun onUserStartedApp(isLoggedIn: Boolean) {
        if (isLoggedIn) {
            view?.showLayout(PageType.Main)
            Log.e("Lasha", "User is Signed IN!")
        } else {
            view?.showLayout(PageType.Enter)
            Log.e("Lasha", "User is Signed OUT!")
        }
    }

    override fun onUserSigned(successfuly: Boolean, failReason: String, isRegistering: Boolean) {
        if (successfuly) {
            onUserStartedApp(true)
        } else {
            if (isRegistering) {
                view?.showLayout(PageType.Register)
                view?.showToast("Register Failed. $failReason")
            } else {
                view?.showLayout(PageType.Enter)
                view?.showToast("Authenticate Failed. $failReason")
            }
        }
    }

    override fun retrievedUsersBriefInfo(usersBriefInfo: MutableList<UserInfo>) {
        view?.usersBriefInfoReceived(usersBriefInfo)
    }

    override fun userParametersUpdated(successfully: Boolean) {
        if (successfully) {
            view?.showLayout(PageType.Profile)
        } else {
            view?.showLayout(PageType.Profile)
            view?.showToast("Update Failed")
        }
    }
    //endregion
}