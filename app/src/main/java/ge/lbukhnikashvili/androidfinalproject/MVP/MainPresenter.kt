package ge.lbukhnikashvili.androidfinalproject.MVP

import android.app.Application
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import ge.lbukhnikashvili.androidfinalproject.DataClasses.UserInfo
import ge.lbukhnikashvili.androidfinalproject.MainActivity


class MainPresenter(var view: IMainView?) : IMainPresenter {

    private val interactor = MainInteractor(this)

    fun initDatabase(application: Application, activity: MainActivity) {
        interactor.initDatabase(application, activity)
    }

    override fun addUser(uid:String,nickname: String, profession: String, icon: String) {
        interactor.addUser(uid,nickname, profession, icon)
    }

    override fun requestUsersBriefInfo() {
        interactor.requestUsersBriefInfo()
    }

    override fun retrievedUsersBriefInfo(data: DataSnapshot?) {
        var usersBriefInfo: MutableList<UserInfo> = mutableListOf()
        if (data != null) {
            for (item in data.children) {
                var briefInfo= item.getValue(UserInfo::class.java)
                if (briefInfo != null) {
                    usersBriefInfo.add(briefInfo)
                }
            }
            view?.usersBriefInfoReceived(usersBriefInfo)
        } else {
            TODO()
        }
    }
}