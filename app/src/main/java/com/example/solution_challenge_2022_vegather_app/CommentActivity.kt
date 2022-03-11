package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommentBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommentRecyclerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.HashMap

class CommentActivity : AppCompatActivity() {

    val binding by lazy { ActivityCommentBinding.inflate(layoutInflater) }

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var userRef: DocumentReference

    private lateinit var userName: String
    private lateinit var recipeName: String
    private lateinit var commentContainer : CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUiBarColor(isBlack = true)
        getData()

        binding.inputDoneButton.setOnClickListener {
            val inputText : String = binding.commentInputText.text.toString()

            if( isCorrectInput(inputText) ){
                addComment(inputText)
                MyApplication.prefs.setPrefs("Comment", "Done")
            }
            else showNotice("There's no comment.")
        }

        binding.backRecipeBtn.setOnClickListener {
            finish()
        }
    }

    // 1. 데이터 get,set 작업

    private fun getData() {
        recipeName = intent.getStringExtra("recipe").toString()
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        userRef = db.collection("Users").document(user.email.toString())

        // 현재 유저의 닉네임을 불러온다.
        userRef.get().addOnSuccessListener {
            val convertedData = it.toObject(UserForm::class.java)
            userName = convertedData?.NickName.toString()
        }

        getCommentAdapter()
    }

    private fun getCommentAdapter(){
        binding.commentRecycler.layoutManager = LinearLayoutManager(this)
        commentContainer = CommentAdapter(CommentRecyclerBinding.inflate(layoutInflater),recipeName,this)
        commentContainer.setCurrentUserId(user.email.toString())
        binding.commentRecycler.adapter = commentContainer
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTime(): String {
        val now = System.currentTimeMillis()
        return DateFormat.format("yyyy.MM.dd kk:mm:ss",now).toString()
    }

    // 2. 데이터베이스 작업 ( 추가 )

    data class HistoryComment(
        val basicComment : HashMap<String,Int> = HashMap(),
        val communityComment : HashMap<String,Int> = HashMap()
    )

    private fun updateUserHistoryToComment( data : HashMap<String,Int> ){
        userRef.collection("History").document("Comment")
            .update("basicComment",data)
    }

    private fun setHistoryCommentData(list: HashMap<String, Int>): HashMap<String, Int> {
        if (recipeName in list) {
            list[recipeName] = list[recipeName]!! + 1
        } else {
            list[recipeName] = 1
        }
        return list
    }

    private fun addUserHistoryToComment() {
        userRef.collection("History").document("Comment")
            .get()
            .addOnSuccessListener {
                var container = it.toObject(HistoryComment::class.java)?.basicComment
                if (container != null) {
                    container = setHistoryCommentData(container)
                    updateUserHistoryToComment(container)
                }
            }
    }

    private fun createCommentForm(inputText: String): CommentForm {
        return CommentForm(
            documentId = null,
            useremail = user.email.toString(),
            nickname = userName,
            text = inputText,
            like = HashMap(),
            reply = 0,
            timestamp = getCurrentTime()
        )
    }

    private fun addComment( inputText : String){
        val newComment = createCommentForm(inputText)

        db.collection("Recipe").document(recipeName).collection("Comment")
            .add(newComment)
            .addOnSuccessListener {
                addUserHistoryToComment()
                clearFocus()
            }
    }

    // 3. 부가적인 작업 ( 서브 )

    private fun isCorrectInput( inputText : String ) : Boolean {
        return inputText.trim().isNotEmpty()
    }

    private fun showNotice(text : String){
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }

    private fun hideKeyboard(){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.commentInputText.windowToken, 0)
    }

    private fun clearFocus(){
        hideKeyboard()
        binding.commentInputText.text = null
        binding.commentInputText.clearFocus()
    }

    private fun setUiBarColor(isBlack : Boolean) {
        val customUiBar = UiBar(window)
        customUiBar.setNaviBarIconColor(isBlack)
        customUiBar.setStatusBarIconColor(isBlack)
    }
}