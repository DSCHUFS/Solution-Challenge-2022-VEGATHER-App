package com.example.solution_challenge_2022_vegather_app

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityJoinBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class JoinActivity : AppCompatActivity() {
    val binding by lazy { ActivityJoinBinding.inflate(layoutInflater)}
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val uiBar = UiBar(window)
        uiBar.setStatusBarIconColor(isBlack = true)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailWatcher = EmailEditWatcher(binding)
        val pwWatcher = PasswordEditWatcher(binding)
        val pwcWatcher = PasswordCheckEditWatcher(binding)
        val nickWatcher = NickEditWatcher(binding)

        with(binding){
            editTextJoinEmail.addTextChangedListener(emailWatcher)
            editTextJoinPw.addTextChangedListener(pwWatcher)
            editTextJoinPwCh.addTextChangedListener(pwcWatcher)
            editTextJoinNick.addTextChangedListener(nickWatcher)
        }
        binding.btnJoin.setOnClickListener{
            if(checkBlank()){
                checkEmail()
            }else{
                Toast.makeText(this,"Please fill the blank.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnBack.setOnClickListener{
            finish()
        }
    }

    private fun checkBlank(): Boolean {
        with(binding){
            val email = editTextJoinEmail.text.toString()
            val pw = editTextJoinPw.text.toString()
            val pwCh = editTextJoinPwCh.text.toString()
            val nick = editTextJoinNick.text.toString()
            return !(email.isEmpty() || pw.isEmpty() || pwCh.isEmpty() || nick.isEmpty())
        }
    }

    private fun checkEmail(){
        val currentUser : FirebaseUser? = auth.currentUser //현재 로그인한 사용자 가져오기

        if(currentUser != null) { //이메일 유효성 검사 ok
            checkNick()
        }else{
            auth.createUserWithEmailAndPassword(binding.editTextJoinEmail.text.toString(), binding.editTextJoinPw.text.toString())
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        checkNick()
                    } else{ //이미 가입된 사용자
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        binding.emailChComment.visibility = View.VISIBLE
                        binding.emailChIcon.visibility = View.INVISIBLE
                        binding.emailChComment.text = "This email is already registered."
                    }
                }
        }
    }

    //Nickname 중복검사 체크함수
    private fun checkNick(){
        val nick = binding.editTextJoinNick.text.toString()
        var res = true
        val userRef = db.collection("Users")
        if(nick.isEmpty()){
            binding.nickChComment.visibility = View.VISIBLE
        } else {
            userRef.whereEqualTo("NickName", nick)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        if (document.data.get("NickName") == nick) {
                            res = false
                            binding.nickChComment.visibility = View.VISIBLE
                            binding.nickChIcon.visibility = View.INVISIBLE
                            binding.nickChComment.text = "This nickname is already registered."
                            break
                        }
                    }
                    if (res) joinInEmail()
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get fail with", exception)
                }
        }
    }


    private fun joinInEmail() { //이메일로 회원가입
        val user: MutableMap<String, Any> = HashMap()
        user["NickName"] = binding.editTextJoinNick.text.toString()
        user["Email"] = binding.editTextJoinEmail.text.toString()
        user["Point"] = 0
        user["VeganLevel"] = 1
        user["LoginWith"] = "email"

        val newLikeData = hashMapOf(
            "basicRecipe" to ArrayList<String>(),
            "communityRecipe" to ArrayList<String>()
        )

        val newCommentData = hashMapOf(
            "basicComment" to HashMap<String,Int>(),
            "communityComment" to HashMap<String,Int>()
        )

        val newPostingData = hashMapOf(
            "posting" to ArrayList<String>()
        )

        val newSearchData = hashMapOf(
            "basicSearch" to HashMap<String,String>(),
            "communitySearch" to HashMap<String,String>()
        )

        //User DB에 초기 정보 저장
        db.collection("Users").document(binding.editTextJoinEmail.text.toString())
            .set(user)
            .addOnSuccessListener {
                Log.d("createUserInfo","success")
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

        db.collection("Users").document(binding.editTextJoinEmail.text.toString())
            .collection("History")
            .document("Like")
            .set(newLikeData)
            .addOnSuccessListener {
                Log.d("createHistoryLike","success")
            }
            .addOnFailureListener {
                Log.d("createHistoryLike","fail")
            }

        db.collection("Users").document(binding.editTextJoinEmail.text.toString())
            .collection("History")
            .document("Comment")
            .set(newCommentData)
            .addOnSuccessListener {
                Log.d("createHistoryComment","success")
            }
            .addOnFailureListener {
                Log.d("createHistoryComment","fail")
            }

        db.collection("Users").document(binding.editTextJoinEmail.text.toString())
            .collection("History")
            .document("Posting")
            .set(newPostingData)
            .addOnSuccessListener {
                Log.d("createHistoryPosting","success")
            }
            .addOnFailureListener {
                Log.d("createHistoryPosting","fail")
            }

        db.collection("Users").document(binding.editTextJoinEmail.text.toString())
            .collection("History")
            .document("Search")
            .set(newSearchData)
            .addOnSuccessListener {
                Log.d("createHistorySearch","success")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Log.d("createHistorySearch","fail")
            }
    }
}


class EmailEditWatcher(val binding: ActivityJoinBinding) : TextWatcher {
    override fun afterTextChanged(email: Editable?) {
        with(binding) {
            if (email != null) {
                val valid = Patterns.EMAIL_ADDRESS.matcher(email).matches();
//                Log.d("VALID", valid.toString())
                if (valid) {
                    emailChComment.visibility = View.INVISIBLE
                    emailChIcon.visibility = View.VISIBLE
                } else {
                    emailChComment.visibility = View.VISIBLE
                    emailChIcon.visibility = View.INVISIBLE
                    emailChComment.text = "Please use a valid email address."
                    if(email.isEmpty()) emailChComment.visibility = View.INVISIBLE
                }
            } else {
                emailChIcon.visibility = View.INVISIBLE
                emailChComment.visibility = View.INVISIBLE
            }
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
}

class PasswordEditWatcher(val binding: ActivityJoinBinding) : TextWatcher {
    override fun afterTextChanged(password: Editable?) {
        with(binding) {
            if (password != null) {
                if (password.length > 6) {
                    pwChComment.visibility = View.INVISIBLE
                    pwChIcon.visibility = View.VISIBLE
                } else {
                    pwChComment.visibility = View.VISIBLE
                    pwChIcon.visibility = View.INVISIBLE
                    if(password.isEmpty()) pwChComment.visibility = View.INVISIBLE
                }
            } else {
                pwChComment.visibility = View.VISIBLE
                pwChIcon.visibility = View.INVISIBLE
            }
        }
    }
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
}

class PasswordCheckEditWatcher(val binding: ActivityJoinBinding) : TextWatcher {
    override fun afterTextChanged(password: Editable?) {
        with(binding) {
            if (password != null) {
                if (password.toString() == editTextJoinPw.text.toString()) {
                    pwChaIcon.visibility = View.VISIBLE
                    pwChComment2.visibility = View.INVISIBLE
                } else {
                    pwChaIcon.visibility = View.INVISIBLE
                    pwChComment2.visibility = View.VISIBLE
                    pwChComment2.text = "Passwords do not match."
                    if(password.isEmpty()){
                        pwChComment2.visibility = View.INVISIBLE
                        pwChaIcon.visibility = View.INVISIBLE
                    }
                }
            }else {
                pwChaIcon.visibility = View.INVISIBLE
                pwChComment2.visibility = View.VISIBLE
                pwChComment2.text = "Please fill this blank."
            }
        }
    }
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
}

class NickEditWatcher(val binding: ActivityJoinBinding) : TextWatcher {
    override fun afterTextChanged(nick: Editable?) {
        with(binding) {
            if (nick != null) {
                nickChIcon.visibility = View.VISIBLE
                nickChComment.visibility = View.INVISIBLE
                if(nick.isEmpty()){
                    nickChComment.visibility = View.INVISIBLE
                    nickChIcon.visibility = View.INVISIBLE
                }
            } else {
                nickChIcon.visibility = View.INVISIBLE
                nickChComment.visibility = View.VISIBLE
            }
        }
    }
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
}
