package ge.lbukhnikashvili.androidfinalproject.MVP


import android.app.Application
import android.net.Uri
import android.os.AsyncTask
import android.text.format.DateFormat
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
import ge.lbukhnikashvili.androidfinalproject.DataClasses.*
import ge.lbukhnikashvili.androidfinalproject.MainActivity
import ge.lbukhnikashvili.androidfinalproject.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MainInteractor(val presenter: IMainPresenter) {

    private lateinit var database: DatabaseReference
    private lateinit var activity: MainActivity
    private lateinit var application: Application

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    private var currentUser: FirebaseUser? = null
    var currentUserData: User? = null
    var usersBriefInfo: MutableList<UserInfo> = mutableListOf()

    var currentConversationUid = ""
    var currentConversation: Conversation? = null
    var currentMainPageData: MutableList<ConversationInfo> = mutableListOf()

    private var userDataRef:DatabaseReference?=null
    //region Commands
    fun initFirebaseForApplication(application: Application, activity: MainActivity) {
        this.application = application
        this.activity = activity

        database = Firebase.database.reference
        storage = Firebase.storage
        auth = Firebase.auth

        if (auth.currentUser != null) {
            assignCurrentUserAndStartListening()
            presenter.onUserStartedApp(true)
        } else {
            presenter.onUserStartedApp(false)
        }
    }

    private fun assignCurrentUserAndStartListening() {
        currentUser = auth.currentUser
        listenToUserData(currentUser!!.uid)
        RequestNodeFromDatabase(this, database, "users_brief_info").execute()
    }

    fun loginUser(username: String, password: String) {
        val email = "$username@gmail.com"
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
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

        isRequestedMainPage=false
        isRetrievedUsersBriefInfo=false
        isRequestedMainPage=false
        currentUser=null
        currentUserData=null
        usersBriefInfo.clear()
        currentConversationUid=""
        currentConversation=null
        currentMainPageData.clear()
        userDataRef?.removeEventListener(userDataListener)
    }

    fun addUser(uid: String, nickname: String, profession: String, defaultImageUrl: String) {
        val userInfo = UserInfo(uid, nickname, profession, defaultImageUrl)
        database.child("users_brief_info").child(uid).setValue(userInfo)
        val user = User(uid, userInfo, HashMap())
        database.child("users").child(uid).setValue(user)
    }

    private var isRequestedUsersBriefInfo: Boolean = false
    private var isRetrievedUsersBriefInfo: Boolean = false
    fun requestUsersBriefInfo() {
        isRequestedUsersBriefInfo = true
        RequestNodeFromDatabase(this, database, "users_brief_info").execute()
    }

    private var isRequestedMainPage: Boolean = false
    fun requestMainPage() {
        isRequestedMainPage=true
        updateMainPageData()
    }

    fun listenToUserConversation(uid: String) {
        currentConversationUid = uid
        checkCurrentConversationForNewData()
    }

    fun sentUserMessage(message: String) {
        var date = getCurrentDatetime()
        currentConversation?.messages?.set(date, (Message(currentUser?.uid, message, date)))

        database.child("users").child(currentUser?.uid!!).child("conversations")
            .child(currentConversationUid).setValue(currentConversation)
            .addOnSuccessListener {
            }.addOnFailureListener {
            }

        database.child("users").child(currentConversationUid).child("conversations")
            .child(currentUser?.uid!!).setValue(currentConversation)
            .addOnSuccessListener {
            }.addOnFailureListener {
            }
    }
    //endregion


    private fun updateMainPageData() {
        if (isRetrievedUsersBriefInfo==false) return

        currentMainPageData.clear()
        var conversations = currentUserData?.conversations?.toList()
        if (conversations != null) {
            for (conversation in conversations) {
                var messages = conversation.second.messages.toList()
                var message = messages.maxByOrNull { it.second.date!! }
                if (message != null) {
                    var userInfo = presenter.getUserBriefInfo(conversation.first)
                    var info =
                        ConversationInfo(
                            conversation.first, userInfo?.name, message.second.message,
                            userInfo?.image_url, dateToTimeTag(message.second.date)
                        )

                    currentMainPageData.add(info)
                }
            }
        }

        if (isRequestedMainPage){
            isRequestedMainPage=false
            presenter.mainPageDataUpdated(currentMainPageData)
        }
    }

    private fun dateToTimeTag(date: String?): String {
        val date = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS").parse(date)
        val curDate = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS").parse(getCurrentDatetime())
        val diff: Long = curDate.time - date.time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        if (hours < 1) {
            return "$minutes min"
        } else
            if (days < 1) {
                return "$hours hour"
            } else {
                return "" + DateFormat.format("dd", date) as String + " " + DateFormat.format("MMM", date) as String
            }
    }

    private fun checkCurrentConversationForNewData() {
        if (!currentConversationUid.isNullOrBlank()) {
            currentConversation = currentUserData?.conversations?.get(currentConversationUid)
            if (currentConversation == null) {
                currentConversation = Conversation(HashMap())
            }
            presenter.userConversationUpdated(true, currentConversation)
        }
    }

    var userDataListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            currentUserData = dataSnapshot.getValue<User>()
            checkCurrentConversationForNewData()
            updateMainPageData()
        }

        override fun onCancelled(databaseError: DatabaseError) {
        }
    }

    //region Listeners
    private fun listenToUserData(uid: String) {
        userDataRef= database.child("users").child(uid).ref
        userDataRef!!.addValueEventListener(userDataListener)
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
            if (isRequestedUsersBriefInfo) {
                isRequestedUsersBriefInfo=false
                presenter.retrievedUsersBriefInfo(usersBriefInfo)
            }

        } else {
            TODO()
        }

        isRetrievedUsersBriefInfo=true
        updateMainPageData()
    }
    //endregion

    fun getCurrentDatetime(): String {
        return SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS", Locale.getDefault()).format(Date())
    }

    //region Async Classes
    class RequestNodeFromDatabase(
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