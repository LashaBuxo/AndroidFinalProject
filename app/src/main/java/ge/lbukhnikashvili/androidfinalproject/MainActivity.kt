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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        findViewById<View>(R.id.enter_page_layout).visibility = View.VISIBLE
        findViewById<View>(R.id.register_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.main_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.profile_page_layout).visibility = View.INVISIBLE
        auth = Firebase.auth

        //findViewById<RecyclerView>(R.id.conversation_thumbnail_items).adapter = DemoAdapter()
    }

    override fun onStart() {
        super.onStart()
        currentUser = auth.currentUser
        if (currentUser != null) {
            userSignedInSuccessfully()
        } else {
            Log.e("Lasha", "Current user is NOT signed in!")
        }
    }

    fun onClickSignUpPage(v: View) {
        findViewById<View>(R.id.enter_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.register_page_layout).visibility = View.VISIBLE
        findViewById<View>(R.id.main_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.profile_page_layout).visibility = View.INVISIBLE
    }
    fun onClickProfilePage(v: MenuItem):Boolean {
        findViewById<View>(R.id.enter_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.register_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.main_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.profile_page_layout).visibility = View.VISIBLE
        return true;
    }
    fun onClickMainPage(v: MenuItem):Boolean {
        findViewById<View>(R.id.enter_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.register_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.main_page_layout).visibility = View.VISIBLE
        findViewById<View>(R.id.profile_page_layout).visibility = View.INVISIBLE
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

    private fun logoutUser(){
        Firebase.auth.signOut()
        findViewById<View>(R.id.enter_page_layout).visibility = View.VISIBLE
        findViewById<View>(R.id.register_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.main_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.profile_page_layout).visibility = View.INVISIBLE
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

    private fun userSignedInSuccessfully() {
        Log.e("Lasha", "Current user is signed in!")
        findViewById<View>(R.id.enter_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.register_page_layout).visibility = View.INVISIBLE
        findViewById<View>(R.id.main_page_layout).visibility = View.VISIBLE
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