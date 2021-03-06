package ge.lbukhnikashvili.androidfinalproject

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import de.hdodenhof.circleimageview.CircleImageView
import ge.lbukhnikashvili.androidfinalproject.Adapters.ConversationPageAdapter
import ge.lbukhnikashvili.androidfinalproject.Adapters.MainPageAdapter
import ge.lbukhnikashvili.androidfinalproject.Adapters.SearchPageAdapter
import ge.lbukhnikashvili.androidfinalproject.DataClasses.Conversation
import ge.lbukhnikashvili.androidfinalproject.DataClasses.ConversationInfo
import ge.lbukhnikashvili.androidfinalproject.DataClasses.Message
import ge.lbukhnikashvili.androidfinalproject.DataClasses.UserInfo
import ge.lbukhnikashvili.androidfinalproject.MVP.IMainView
import ge.lbukhnikashvili.androidfinalproject.MVP.MainPresenter


class MainActivity : AppCompatActivity(), IMainView {

    //region Variables
    private val pickImageRequestCode = 100

    private lateinit var presenter: MainPresenter
    private var lastLoadedImageUri: Uri? = null
    //endregion

    //region Activity Life-Cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        this.window.setSoftInputMode(SOFT_INPUT_ADJUST_PAN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        showLayout(PageType.Loading)

        // UNIVERSAL IMAGE LOADER SETUP
        val defaultOptions: DisplayImageOptions = DisplayImageOptions.Builder()
            .resetViewBeforeLoading(true)
            .cacheOnDisk(true)
            .cacheInMemory(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .displayer(FadeInBitmapDisplayer(300))
            .build()

        val config: ImageLoaderConfiguration = ImageLoaderConfiguration.Builder(applicationContext)
            .defaultDisplayImageOptions(defaultOptions)
            .memoryCache(WeakMemoryCache())
            .diskCacheSize(100 * 1024 * 1024)
            .build()

        ImageLoader.getInstance().init(config)
        // END - UNIVERSAL IMAGE LOADER SETUP

        presenter = MainPresenter(this)
        presenter.initPresenter(application, this)

        var searchView1 = findViewById<SearchView>(R.id.users_search_view)
        var searchView2 = findViewById<SearchView>(R.id.conversations_search_view)

        searchView1.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank() || newText.length > 2) {
                    if (newText.isNullOrBlank()) onUserSearchTextChanged("");
                    else
                        onUserSearchTextChanged(newText)
                }
                return true
            }
        }
        )
        searchView2.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank() || newText.length > 2) {
                    if (newText.isNullOrBlank()) onConversationsSearchTextChanged("");
                    else
                        onConversationsSearchTextChanged(newText)
                }
                return true
            }
        }
        )
    }


    //endregion

    //region UserInteraction
    private var FirstTime: Boolean = false
    fun onUserSearchTextChanged(text: String) {
        if (!FirstTime) {
            FirstTime = true;
            return
        }
        var newData = filterUsersBriefDataBySearch(savedBriefInfo, text)
        showBriefInfoData(newData)
    }

    private var FirstTime2: Boolean = false
    fun onConversationsSearchTextChanged(text: String) {
        if (!FirstTime2) {
            FirstTime2 = true;
            return
        }

        var newData = filterMainPageDataBySearch(savedMainPageData, text)
        showMainPageData(newData)
    }

    fun onClickSignUpPage(v: View) {
        showLayout(PageType.Register)
    }

    fun onClickProfilePage(v: MenuItem): Boolean {
        showLayout(PageType.Loading)
        val currentUserData = presenter.getCurrentUserData()

        findViewById<TextView>(R.id.profile_page_nickname).setText(currentUserData?.info?.name)
        findViewById<EditText>(R.id.profile_page_profession).setText(currentUserData?.info?.profession)

        var listener = object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(
                imageUri: String?,
                view: View?,
                loadedImage: Bitmap?
            ) {
                super.onLoadingComplete(imageUri, view, loadedImage)
                showLayout(PageType.Profile)
            }

            override fun onLoadingFailed(
                imageUri: String?,
                view: View?,
                failReason: FailReason?
            ) {
                super.onLoadingFailed(imageUri, view, failReason)
                showLayout(PageType.Profile)
            }
        }

        Log.e("Lasha", "" + (currentUserData == null))
        Log.e("Lasha", "" + (currentUserData?.info == null))
        Log.e("Lasha", "" + (currentUserData?.info?.image_url == null))

        ImageLoader.getInstance().displayImage(
            currentUserData!!.info?.image_url,
            findViewById<CircleImageView>(R.id.profile_page_icon),
            DisplayImageOptions.createSimple(),
            listener
        );

        return true;
    }

    fun onClickActionButton(v: View) {
        showLayout(PageType.Loading)
        presenter.requestUsersBriefInfo()
    }

    fun onClickMainPage(v: MenuItem): Boolean {
        prepareForOpenMainPage()
        return true
    }

    fun onClickUpdateProfile(v: View) {
        showLayout(PageType.Loading)
        var newName = findViewById<TextView>(R.id.profile_page_nickname).text.toString()
        var newProfession = findViewById<EditText>(R.id.profile_page_profession).text.toString()
        presenter.updateUserParameters(newName, newProfession, lastLoadedImageUri)
    }

    fun onClickSignIn(v: View) {
        showLayout(PageType.Loading)
        val nicknameText = findViewById<EditText>(R.id.enter_page_nickname).text.toString()
        val passwordText = findViewById<EditText>(R.id.enter_page_password).text.toString()
        presenter.loginUser(nicknameText, passwordText)
    }

    fun onClickSignOut(v: View) {
        findViewById<EditText>(R.id.enter_page_nickname).setText("")
        findViewById<EditText>(R.id.enter_page_password).setText("")
        findViewById<EditText>(R.id.register_page_nickname).setText("")
        findViewById<EditText>(R.id.register_page_password).setText("")
        findViewById<EditText>(R.id.register_page_profession).setText("")
        presenter.logoutUser()
    }

    fun onClickSignUp(v: View) {
        showLayout(PageType.Loading)
        val nicknameText = findViewById<EditText>(R.id.register_page_nickname).text.toString()
        val passwordText = findViewById<EditText>(R.id.register_page_password).text.toString()
        val professionText = findViewById<EditText>(R.id.register_page_profession).text.toString()
        presenter.registerUser(nicknameText, passwordText, professionText)
    }

    fun onClickProfileImage(v: View) {
        showLayout(PageType.Loading)
        if (askForPermissions()) {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImageRequestCode)
        }
    }

    fun onClickSearchPageBack(v: View) {
        prepareForOpenMainPage()
    }

    private fun onClickSearchItem(uid: String) {
        var userBriefInfo = presenter.getUserBriefInfo(uid)
        if (userBriefInfo == null) {
            showToast(getString(R.string.fail_unexpected))
            return
        }
        findViewById<TextView>(R.id.conversation_page_name).text = userBriefInfo.name
        findViewById<TextView>(R.id.conversation_page_profession).text = userBriefInfo.profession

        ImageLoader.getInstance().displayImage(
            userBriefInfo.image_url,
            findViewById<CircleImageView>(R.id.conversation_page_item_icon),
            DisplayImageOptions.createSimple(),
            null
        );


        //inform presenter about conversation open
        //and wait conversation to load
        showLayout(PageType.Loading)
        presenter.openUserConversation(uid)
    }

    fun onClickSendMessage(v: View) {
        var message = findViewById<TextView>(R.id.conversation_typed_message).text.toString()
        presenter.sentUserMessage(message)
        findViewById<TextView>(R.id.conversation_typed_message).text = ""
    }

//endregion

    //region Permissions
    fun isPermissionsAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun askForPermissions(): Boolean {
        if (!isPermissionsAllowed()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this as Activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                showPermissionDeniedDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this as Activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    pickImageRequestCode
                )
            }
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            pickImageRequestCode -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission is granted, you can perform your operation here
                } else {
                    // permission is denied, you can ask for permission again, if you want
                    //  askForPermissions()
                }
                return
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Permission is denied, Please allow permissions from App Settings.")
            .setPositiveButton("App Settings",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    // send to app settings if permission is denied permanently
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", getPackageName(), null)
                    intent.data = uri
                    startActivity(intent)
                })
            .setNegativeButton("Cancel", null)
            .show()
    }
    //endregion

    //region methods

    override fun showToast(message: String) {
        Toast.makeText(
            baseContext, message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun filterUsersBriefDataBySearch(
        data: MutableList<UserInfo>,
        text: String
    ): MutableList<UserInfo> {
        var newData: MutableList<UserInfo> = mutableListOf()
        for (info in data) {
            if (text.length < 3 || info.name!!.contains(text)) {
                newData.add(info)
            }
        }
        return newData
    }

    private fun filterMainPageDataBySearch(
        data: MutableList<ConversationInfo>,
        text: String
    ): MutableList<ConversationInfo> {
        var newData: MutableList<ConversationInfo> = mutableListOf()
        for (info in data) {
            if (text.length < 3 || info.toName!!.contains(text)) {
                newData.add(info)
            }
        }
        return newData
    }

    //endregion

    //region callbacks
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImageRequestCode) {
            if (resultCode == RESULT_OK) {
                var imageUri = data?.data
                findViewById<CircleImageView>(R.id.profile_page_icon).setImageURI(imageUri)
                lastLoadedImageUri = imageUri
            } else {
                Toast.makeText(
                    baseContext, "ImageLoad Failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
            showLayout(PageType.Profile)
        }
    }


    var savedBriefInfo: MutableList<UserInfo> = mutableListOf()
    override fun usersBriefInfoReceived(data: MutableList<UserInfo>) {
        savedBriefInfo = data
        showBriefInfoData(savedBriefInfo)
    }

    fun showBriefInfoData(data: MutableList<UserInfo>) {
        val adapter = SearchPageAdapter(data)
        var recycler = findViewById<RecyclerView>(R.id.search_items)
        recycler.adapter = adapter
        recycler.layoutManager = GridLayoutManager(baseContext, GridLayoutManager.VERTICAL)
        adapter.onItemClick = { uid -> onClickSearchItem(uid) }

        showLayout(PageType.Search)
    }

    override fun userConversationUpdated(successfully: Boolean, conversation: Conversation?) {
        Log.e("Lasha", "userConversationUpdated")
        if (!successfully) {
            prepareForOpenMainPage()
            showToast(getString(R.string.fail_unexpected))
            return
        }
        showLayout(PageType.Conversation)
        val adapter = ConversationPageAdapter(conversation!!, presenter.getCurrentUserData()?.uid!!)
        var recycler = findViewById<RecyclerView>(R.id.conversation_items)
        recycler.adapter = adapter
        var layoutManager = LinearLayoutManager(baseContext)
        layoutManager.stackFromEnd = true
        recycler.layoutManager = layoutManager
    }


    var savedMainPageData: MutableList<ConversationInfo> = mutableListOf()
    override fun mainPageDataUpdated(conversationsInfo: MutableList<ConversationInfo>) {
        savedMainPageData = conversationsInfo
        showMainPageData(savedMainPageData)
    }

    fun showMainPageData(conversationsInfo: MutableList<ConversationInfo>) {
        val adapter = MainPageAdapter(conversationsInfo)
        var recycler = findViewById<RecyclerView>(R.id.conversation_thumbnail_items)
        recycler.adapter = adapter
        var layoutManager = LinearLayoutManager(baseContext)
        recycler.layoutManager = layoutManager
        adapter.onItemClick = { uid -> onClickSearchItem(uid) }

        showLayout(PageType.Main)
    }

    override fun prepareForOpenMainPage() {
        presenter.requestMainPage()
    }
    //endregion

    //region Layouts Switching
    enum class PageType {
        Loading, Enter, Register, Main, Profile, Search, Conversation
    }

    override fun showLayout(page: PageType) {
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
            PageType.Register -> findViewById<View>(R.id.register_page_layout).visibility =
                View.VISIBLE
            PageType.Main -> findViewById<View>(R.id.main_page_layout).visibility = View.VISIBLE
            PageType.Profile -> findViewById<View>(R.id.profile_page_layout).visibility =
                View.VISIBLE
            PageType.Search -> findViewById<View>(R.id.search_page_layout).visibility = View.VISIBLE
            PageType.Conversation -> findViewById<View>(R.id.conversation_page_layout).visibility =
                View.VISIBLE
        }
    }
    //endregion

    //region add-on functional
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
    //endregion
}