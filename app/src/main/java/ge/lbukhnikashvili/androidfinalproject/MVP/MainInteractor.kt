package ge.lbukhnikashvili.androidfinalproject.MVP


import android.app.Application
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ge.lbukhnikashvili.androidfinalproject.DataClasses.User
import ge.lbukhnikashvili.androidfinalproject.DataClasses.UserInfo
import ge.lbukhnikashvili.androidfinalproject.MainActivity

class MainInteractor(val presenter: IMainPresenter) {

    private lateinit var database: DatabaseReference

    fun initDatabase(application: Application, activity: MainActivity) {
        database = Firebase.database.reference


    }

    fun addUser(uid: String, nickname: String, profession: String, icon: String) {
        val userInfo = UserInfo(uid, nickname, profession, icon)
        database.child("users_brief_info").child(uid).setValue(userInfo)

        val user = User(uid, userInfo, mutableListOf())
        database.child("users").child(uid).setValue(user)
    }

    fun requestUsersBriefInfo() {
        RequestUsersBriefInfoTask(presenter, database, "users_brief_info").execute()
    }

    class RequestUsersBriefInfoTask(
        private val presenter: IMainPresenter,
        val database: DatabaseReference,
        private val path: String
    ) : AsyncTask<Void, Void, Boolean?>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            database.child(path).get().addOnSuccessListener {
                presenter.retrievedUsersBriefInfo(it)
            }.addOnFailureListener {
                Log.e("Lasha_usersBriefInfo", it.message + " " + it.cause + " " + it.stackTrace)
                presenter.retrievedUsersBriefInfo(null)
            }
            return true;
        }
    }

}