package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Adapter
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.MimeTypeFilter.matches
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommentBinding
import com.example.solution_challenge_2022_vegather_app.databinding.ActivitySearchBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommentRecyclerBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import java.util.regex.Pattern.matches
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class CommentActivity : AppCompatActivity() {

    val binding by lazy { ActivityCommentBinding.inflate(layoutInflater) }

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var userRef: DocumentReference

    private lateinit var userName: String
    private lateinit var recipeName: String
    private var commentCount = 0
    private val commentList = ArrayList<CommentForm>()

    private lateinit var commentContainer : CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        getData()

        db.collection("Recipe").document(recipeName)
            .addSnapshotListener { value, error ->
                val convertedData = value?.toObject(RecipeInformation::class.java)
                commentCount = convertedData?.comment!!
                binding.commentCount.text = commentCount.toString()
                Log.d("댓글 수 ->", commentCount.toString())
        }

        setNavIconColor(isBlack = true)

        binding.backRecipeBtn.setOnClickListener {
            finish()
        }

        binding.inputDoneButton.setOnClickListener {
            val inputText : String = binding.commentInputText.text.toString()
            if( isCorrectInput(inputText) ){
                addComment(inputText)
            }
            else{
                showNotice("There's no comment.")
            }
        }
    }

    private fun getData() {
        recipeName = intent.getStringExtra("recipe").toString()
        commentCount = intent.getIntExtra("commentCount",0)
        binding.commentCount.text = commentCount.toString()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        userRef = db.collection("Users").document(user.email.toString())

        // 현재 유저의 닉네임을 불러온다.
        userRef.get().addOnSuccessListener {
            val convertedData = it.toObject(UserForm::class.java)
            userName = convertedData?.NickName.toString()
        }

        // 레시피에 대한 댓글을 전부 가져온다.
        db.collection("Recipe").document(recipeName)
            .collection("Comment")
            .get()
            .addOnSuccessListener {
                for ( document in it){
                    commentList.add(document.toObject(CommentForm::class.java))
                }
                connectCommentAdapter()
            }
    }

    private fun setNavIconColor(isBlack : Boolean) {
        val customUiBar = UiBar(window)
        customUiBar.setNaviBarIconColor(isBlack)
    }

    private fun isCorrectInput( inputText : String ) : Boolean {
        return inputText.trim().isNotEmpty()
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTime(): String {
        val now = System.currentTimeMillis()
        val date = Date(now)
        val dateFormat = SimpleDateFormat("yyyy.MM.dd")
        return dateFormat.format(date)
    }

    private fun connectCommentAdapter(){
        binding.commentRecycler.layoutManager = LinearLayoutManager(this)
        commentContainer = CommentAdapter(CommentRecyclerBinding.inflate(layoutInflater))
        commentContainer.setData(commentList,recipeName)
        commentContainer.loadParentActivity(this)
        binding.commentRecycler.adapter = commentContainer
    }

    private fun addComment( inputText : String){
        val newComment = CommentForm(
            useremail = user.email.toString(),
            nickname = userName,
            text = inputText,
            like = 0,
            reply = 0,
            timestamp = getCurrentTime()
        )
        db.collection("Recipe").document(recipeName).collection("Comment")
            .add(newComment)
            .addOnSuccessListener {
                Log.d("댓글 입력 후 갱신","성공")
                commentContainer.addComment(newComment)
                db.collection("Recipe").document(recipeName)
                    .update("comment",commentCount+1)
                    .addOnSuccessListener {
                        Log.d("레시피 코멘트 수 업데이트","성공")
                    }
                clearFocus()
            }


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
}