package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CommentReplyActivity : AppCompatActivity() {

    val binding by lazy { ActivityCommentReplyBinding.inflate(layoutInflater) }

    private lateinit var userName: String
    private lateinit var recipeName : String
    private lateinit var commentInfo : CommentForm

    private lateinit var db : FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var userRef: DocumentReference

    private lateinit var commentContainer : CommentReplyAdapter
    private val replyCommentList = ArrayList<CommentForm>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setNavIconColor(isBlack = true)

        getData()

        db.collection("Recipe").document(recipeName).collection("Comment")
            .document(commentInfo.documentId.toString())
            .addSnapshotListener { value, error ->
                val latestData = value?.toObject(CommentForm::class.java)
                commentInfo.reply = latestData?.reply
            }

        binding.imageButton3.setOnClickListener {
            finish()
        }

        showReplyComments()

        binding.replyInputDoneBtn.setOnClickListener {
            val inputText : String = binding.replyCommentInputText.text.toString()

            if( isCorrectInput(inputText) ){
                addComment(inputText)
            }
            else{
                showNotice("There's no comment.")
            }
        }
    }

    private fun setReplyCommentData(){
        db.collection("Recipe").document(recipeName).collection("Comment")
            .document(commentInfo.documentId!!)
            .collection("Reply")
            .get()
            .addOnSuccessListener {
                for (document in it){
                    replyCommentList.add(document.toObject(CommentForm::class.java))
                }
                showReplyComments()
            }
    }

    private fun getData(){
        commentInfo = intent.getParcelableExtra<CommentForm>("commentInfo")!!
        recipeName = intent.getStringExtra("recipeName").toString()
        commentContainer = CommentReplyAdapter(CommentReplyRecyclerBinding.inflate(layoutInflater))

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        userRef = db.collection("Users").document(user.email.toString())

        // 현재 유저의 닉네임 저장
        userRef.get().addOnSuccessListener {
            val convertedData = it.toObject(UserForm::class.java)
            userName = convertedData?.NickName.toString()
        }

        binding.name.text = commentInfo.nickname
        binding.userText.text = commentInfo.text
        binding.time.text = commentInfo.timestamp

        // 답글 리스트를 불러와서 화면에 출력
        if( commentInfo.documentId!=null ){
            setReplyCommentData()
        }
    }

    private fun addComment( inputText : String){
        val newComment = CommentForm(
            useremail = user.email.toString(),
            nickname = userName,
            text = inputText,
            like = 0,
            reply = null,
            timestamp = getCurrentTime()
        )

        db.collection("Recipe").document(recipeName).collection("Comment")
            .document(commentInfo.documentId.toString())
            .collection("Reply")
            .add(newComment)
            .addOnSuccessListener {
                commentContainer.addReplyComment(newComment)

                db.collection("Recipe").document(recipeName)
                    .collection("Comment")
                    .document(commentInfo.documentId.toString())
                    .update("reply", commentInfo.reply?.plus(1))

                clearFocus()
            }
    }

    private fun setNavIconColor(isBlack : Boolean) {
        val customUiBar = UiBar(window)
        customUiBar.setNaviBarIconColor(isBlack)
    }

    private fun isCorrectInput( inputText : String ) : Boolean {
        return inputText.trim().isNotEmpty()
    }

    private fun showReplyComments(){
        binding.replyRecycler.layoutManager = LinearLayoutManager(this)
        commentContainer.setData(replyCommentList,recipeName)
        binding.replyRecycler.adapter = commentContainer
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTime(): String {
        val now = System.currentTimeMillis()
        val date = Date(now)
        val dateFormat = SimpleDateFormat("yyyy.MM.dd")
        return dateFormat.format(date)
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