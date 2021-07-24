package ge.lbukhnikashvili.androidfinalproject

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ge.lbukhnikashvili.androidfinalproject.Adapters.SearchPageAdapter
import ge.lbukhnikashvili.androidfinalproject.DataClasses.UserInfo
import ge.lbukhnikashvili.androidfinalproject.MVP.IMainView
import ge.lbukhnikashvili.androidfinalproject.MVP.MainPresenter


class MainActivity : AppCompatActivity(), IMainView {
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    private lateinit var presenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        showLayout(PageType.Loading)
    }

    override fun onStart() {
        super.onStart()
        auth = Firebase.auth
        currentUser = auth.currentUser

        if (currentUser != null) {
            userSignedInSuccessfully()
        } else {
            showLayout(PageType.Enter)
            Log.e("Lasha", "Current user is NOT signed in!")
        }

        presenter = MainPresenter(this)
        presenter.initDatabase(application, this)
    }

    fun onClickSignUpPage(v: View) {
        showLayout(PageType.Register)
    }

    fun onClickProfilePage(v: MenuItem): Boolean {
        showLayout(PageType.Profile)
        return true;
    }

    fun onClickActionButton(v: View) {
        showLayout(PageType.Loading)
        presenter.requestUsersBriefInfo()
    }

    fun onClickMainPage(v: MenuItem): Boolean {
        showLayout(PageType.Main)
        return true;
    }

    fun onClickUpdateProfile(v: View) {

    }

    fun onClickSignIn(v: View) {
        val nicknameText = findViewById<EditText>(R.id.enter_page_nickname)
        val passwordText = findViewById<EditText>(R.id.enter_page_password)
        loginUser(nicknameText.text.toString(), passwordText.text.toString())
    }

    fun onClickSignOut(v: View) {
        logoutUser();
    }

    fun onClickSignUp(v: View) {
        val nicknameText = findViewById<EditText>(R.id.register_page_nickname)
        val passwordText = findViewById<EditText>(R.id.register_page_password)
        registerUser(nicknameText.text.toString(), passwordText.text.toString())
    }

    private fun onClickSearchItem(uid: String){
        Log.e("Lasha","Clicked on user with uid="+uid)
    }

    private fun logoutUser() {
        Firebase.auth.signOut()
        showLayout(PageType.Enter)
    }

    private fun loginUser(username: String, password: String) {
        var email = "$username@gmail.com"
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Lasha", "signInWithEmail:success")
                    currentUser = auth.currentUser
                    userSignedInSuccessfully();
                } else {
                    Log.w("Lasha", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun registerUser(username: String, password: String) {
        var email = "$username@gmail.com"
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Lasha", "createUserWithEmail:success")
                    currentUser = auth.currentUser
                    val profession =
                        findViewById<EditText>(R.id.register_page_profession).text.toString()
                    presenter.addUser(currentUser!!.uid, username, profession, "")
                    userSignedInSuccessfully();
                } else {
                    Log.w("Lasha", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    override fun usersBriefInfoReceived(data: MutableList<UserInfo>) {
        val adapter = SearchPageAdapter(data)
        var recycler = findViewById<RecyclerView>(R.id.search_items)
        recycler.adapter = adapter
        recycler.layoutManager = GridLayoutManager(baseContext, GridLayoutManager.VERTICAL)
        adapter.onItemClick = { uid -> onClickSearchItem(uid) }

        showLayout(PageType.Search)
    }

    private fun userSignedInSuccessfully() {
        Log.e("Lasha", "Current user is signed in!")
        showLayout(PageType.Main)
    }

    enum class PageType {
        Loading,Enter,Register,Main,Profile,Search,Conversation
    }

    private fun showLayout(page: PageType){
        findViewById<View>(R.id.loading_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.enter_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.register_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.main_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.profile_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.search_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.conversation_page_layout).visibility = View.INVISIBLE

        when (page) {
            PageType.Loading -> findViewById<View>(R.id.loading_layout).visibility = View.VISIBLE
            PageType.Enter -> findViewById<View>(R.id.enter_page_layout).visibility = View.VISIBLE
            PageType.Register -> findViewById<View>(R.id.register_page_layout).visibility = View.VISIBLE
            PageType.Main -> findViewById<View>(R.id.main_page_layout).visibility = View.VISIBLE
            PageType.Profile -> findViewById<View>(R.id.profile_page_layout).visibility = View.VISIBLE
            PageType.Search -> findViewById<View>(R.id.search_page_layout).visibility = View.VISIBLE
            PageType.Conversation ->  findViewById<View>(R.id.conversation_page_layout).visibility = View.VISIBLE
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

}