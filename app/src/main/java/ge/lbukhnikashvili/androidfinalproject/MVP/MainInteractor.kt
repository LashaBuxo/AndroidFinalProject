package ge.lbukhnikashvili.androidfinalproject.MVP


import android.app.Application
import android.content.res.Resources
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import ge.lbukhnikashvili.androidfinalproject.DataClasses.User
import ge.lbukhnikashvili.androidfinalproject.DataClasses.UserInfo
import ge.lbukhnikashvili.androidfinalproject.MainActivity
import ge.lbukhnikashvili.androidfinalproject.R

class MainInteractor(val presenter: IMainPresenter) {

    private lateinit var activity: MainActivity
    private lateinit var database: DatabaseReference

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    private var currentUser: FirebaseUser? = null
    var currentUserData: User? = null
    var usersBriefInfo: MutableList<UserInfo> = mutableListOf()
    private lateinit var application: Application;

    //region Commands
    fun initFirebaseForApplication(application: Application, activity: MainActivity) {
        this.application = application
        this.activity = activity

        database = Firebase.database.reference
        storage = Firebase.storage
        auth = Firebase.auth

        if (currentUser != null) {
            assignCurrentUserAndStartListening()
            presenter.onUserStartedApp(true)
        } else {
            presenter.onUserStartedApp(false)
        }
    }

    fun assignCurrentUserAndStartListening(){
        currentUser=auth.currentUser
        listenToUserData(currentUser!!.uid)
    }

    fun loginUser(username: String, password: String) {
        val email = "$username@gmail.com"
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful){
                    assignCurrentUserAndStartListening()
                }
                presenter.onUserSigned(task.isSuccessful, "", false)
            }
    }

    fun registerUser(username: String, password: String, profession: String) {
        val email = "$username@gmail.com"
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                var explanation = ""
                if (task.isSuccessful) {
                    assignCurrentUserAndStartListening()
                    addUser(
                        currentUser!!.uid,
                        username,
                        profession,
                        activity.getString(R.string.default_avatar_url)
                    )
                } else {
                    if (task.exception != null) explanation = task.exception!!.message.toString()
                }
                presenter.onUserSigned(task.isSuccessful, explanation, true)
            }
    }

    fun logoutUser() {
        Firebase.auth.signOut()
    }

    fun addUser(uid: String, nickname: String, profession: String, defaultImageUrl: String) {
        val userInfo = UserInfo(uid, nickname, profession, defaultImageUrl)
        database.child("users_brief_info").child(uid).setValue(userInfo)

        val user = User(uid, userInfo, mutableListOf())
        database.child("users").child(uid).setValue(user)
    }

    fun requestUsersBriefInfo() {
        RequestUsersBriefInfoTask(this, database, "users_brief_info").execute()
    }
    //endregion

    //region Listeners
    private fun listenToUserData(uid: String) {
        var userDataRef = database.child("users").child(uid).ref

        val userDataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                currentUserData = dataSnapshot.getValue<User>()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }

        userDataRef.addValueEventListener(userDataListener)
    }
    //endregion

    //region Methods
    fun updateUserParameters(newName: String, newProfession: String, uploadedImageUri: Uri?) {
        val newUserInfo = UserInfo()
        newUserInfo.uid = currentUserData?.uid
        newUserInfo.name = newName
        newUserInfo.profession = newProfession
        newUserInfo.image_url = currentUserData?.info?.image_url

        if (uploadedImageUri != null) {
            val imageReference = storage.reference.child(currentUserData?.uid + ".png")
            var uploadTask = imageReference.putFile(uploadedImageUri)
            uploadTask.addOnFailureListener {
                presenter.userParametersUpdated(false)
            }.addOnSuccessListener {
                imageReference.downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        newUserInfo.image_url = it.result.toString()
                        updateUserInfo(newUserInfo)
                    } else {
                        presenter.userParametersUpdated(false)
                    }
                }
            }
        } else {
            updateUserInfo(newUserInfo);
        }

    }

    private fun updateUserInfo(userInfo: UserInfo) {
        database.child("users_brief_info").child(userInfo.uid!!).setValue(userInfo)
            .addOnSuccessListener {
                database.child("users").child(userInfo.uid!!).child("info").setValue(userInfo)
                    .addOnSuccessListener {
                        presenter.userParametersUpdated(true)
                    }.addOnFailureListener {
                        presenter.userParametersUpdated(false)
                    }
            }.addOnFailureListener {
                presenter.userParametersUpdated(false)
            }
    }

    //endregion

    //region local Callbacks
    fun retrievedUsersBriefInfo(data: DataSnapshot?) {
        usersBriefInfo = mutableListOf()
        if (data != null) {
            for (item in data.children) {
                var briefInfo = item.getValue(UserInfo::class.java)
                if (briefInfo != null) {
                    usersBriefInfo.add(briefInfo)
                }
            }
            presenter.retrievedUsersBriefInfo(usersBriefInfo)
        } else {
            TODO()
        }
    }
    //endregion

    //region Async Classes
    class RequestUsersBriefInfoTask(
        private val interactor: MainInteractor,
        val database: DatabaseReference,
        private val path: String
    ) : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            database.child(path).get().addOnSuccessListener {
                interactor.retrievedUsersBriefInfo(it)
            }.addOnFailureListener {
                interactor.retrievedUsersBriefInfo(null)
            }
            return true
        }

    }
    //endregion
}