package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommentReplyBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommentRecyclerBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommentReplyRecyclerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CommentReplyActivity : AppCompatActivity() {

    val binding by lazy { ActivityCommentReplyBinding.inflate(layoutInflater) }

    private lateinit var userName: String
    private lateinit var recipeName : String
    private lateinit var commentInfo : CommentForm

    private lateinit var db : FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var userRef: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setNavIconColor(isBlack = true)

        getData()
        getReplyComments()

        binding.replyInputDoneBtn.setOnClickListener {
            val inputText : String = binding.replyCommentInputText.text.toString()
            if( isCorrectInput(inputText) ){
                addComment(inputText)
                MyApplication.prefs.setPrefs("Comment", "Done")
                MyApplication.prefs.setIntPrefs("commentNum", MyApplication.prefs.getIntPrefs("commentNum", 0)+1)
            }
            else showNotice("There's no comment.")
        }

        binding.imageButton3.setOnClickListener {
            finish()
        }
    }

    // 1.데이터 get,set 작업

    private fun getData(){
        commentInfo = intent.getParcelableExtra<CommentForm>("commentInfo")!!
        recipeName = intent.getStringExtra("recipeName").toString()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        userRef = db.collection("Users").document(user.email.toString())

        userRef.get().addOnSuccessListener {
            val convertedData = it.toObject(UserForm::class.java)
            userName = convertedData?.NickName.toString()
        }
    }

    private fun getReplyComments(){
        val commentAdapter= CommentReplyAdapter(CommentReplyRecyclerBinding.inflate(layoutInflater),
            recipeName,
            user.email.toString(),
            commentInfo.documentId.toString())
        commentAdapter.setContext(this)

        binding.replyRecycler.layoutManager = LinearLayoutManager(this)
        binding.replyRecycler.adapter = commentAdapter
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTime(): String {
        val now = System.currentTimeMillis()
        return DateFormat.format("yyyy.MM.dd kk:mm:ss",now).toString()
    }

    // 2. 데이터베이스 작업

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
            .document(commentInfo.documentId.toString())
            .collection("Reply")
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

    private fun setNavIconColor(isBlack : Boolean) {
        val customUiBar = UiBar(window)
        customUiBar.setStatusBarIconColor(isBlack)
    }

    private fun showNotice(text : String){
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }

    private fun hideKeyboard(){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.replyCommentInputText.windowToken, 0)
    }

    private fun clearFocus(){
        hideKeyboard()
        binding.replyCommentInputText.text = null
        binding.replyCommentInputText.clearFocus()
    }
}