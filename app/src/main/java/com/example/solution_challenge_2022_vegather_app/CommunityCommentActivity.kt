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
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommentBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommentRecyclerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class CommunityCommentActivity : AppCompatActivity() {

    val binding by lazy { ActivityCommentBinding.inflate(layoutInflater) }

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var userRef: DocumentReference

    private lateinit var userName: String
    private lateinit var documentName: String
    private lateinit var commentContainer : CommunityCommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUiBarColor(isBlack = true)
        getData()
        
        binding.inputDoneButton.setOnClickListener {
            val inputText : String = binding.commentInputText.text.toString()

            if( isCorrectInput(inputText) ) addComment(inputText)
            else showNotice("There's no comment.")
        }
        binding.backRecipeBtn.setOnClickListener {
            finish()
        }
    }

    //data get
    private fun getData() {
        documentName = intent.getStringExtra("document name").toString()
        Log.d("Comment : get document name", documentName)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        userRef = db.collection("Users").document(user.email.toString())

        userRef.get().addOnSuccessListener {
            val convertedData = it.toObject(UserForm::class.java)
            Log.d("convertedComment", convertedData.toString())
            userName = convertedData?.NickName.toString()
        }

        getCommentAdapter()
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTime(): String {
        val now = System.currentTimeMillis()
        return DateFormat.format("yyyy.MM.dd kk:mm:ss",now).toString()
    }

    private fun getCommentAdapter(){
        binding.commentRecycler.layoutManager = LinearLayoutManager(this)
        commentContainer = CommunityCommentAdapter(CommentRecyclerBinding.inflate(layoutInflater),documentName,this)
        commentContainer.setCurrentUserId(user.email.toString())
        binding.commentRecycler.adapter = commentContainer
    }
    
    private fun addComment( inputText : String){
        val newComment = createCommentForm(inputText)

        db.collection("Post").document(documentName).collection("Comment")
            .add(newComment)
            .addOnSuccessListener {
                addUserHistoryToComment()
                clearFocus()
            }
    }

    private fun addUserHistoryToComment() {
        userRef.collection("History").document("Comment")
            .get()
            .addOnSuccessListener {
                var container = it.toObject(CommentActivity.HistoryComment::class.java)?.communityComment
                if (container != null) {
                    container = setHistoryCommentData(container)
                    updateUserHistoryToComment(container)
                }
            }
    }

    private fun updateUserHistoryToComment( data : HashMap<String,Int> ){
        userRef.collection("History").document("Comment")
            .update("communityComment",data)
    }

    private fun setHistoryCommentData(list: HashMap<String, Int>): HashMap<String, Int> {
        if (documentName in list) {
            list[documentName] = list[documentName]!! + 1
        } else {
            list[documentName] = 1
        }
        return list
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


    private fun setUiBarColor(isBlack : Boolean) {
        val customUiBar = UiBar(window)
        customUiBar.setNaviBarIconColor(isBlack)
        customUiBar.setStatusBarIconColor(isBlack)
    }

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
}
