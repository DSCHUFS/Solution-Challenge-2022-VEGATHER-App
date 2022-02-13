package com.example.solution_challenge_2022_vegather_app

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityJoinBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class JoinActivity : AppCompatActivity() {
    val binding by lazy { ActivityJoinBinding.inflate(layoutInflater)}
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        binding.btnJoin.setOnClickListener{
            joinInEmail()
        }
    }
    fun checkId(){
        var email = binding.editTextJoinEmail.toString()

    }

    //Password 체크하는 함수
    fun checkPw(){

    }

    //Nickname 중복검사 체크함수
    fun checkNick(){

    }

    fun joinInEmail() {
        Log.d(binding.editTextJoinEmail.text.toString(), binding.editTextJoinPw.text.toString())

        val user: MutableMap<String, Any> = HashMap()
        user["NickName"] = binding.editTextJoinNick.text.toString()
        user["Email"] = binding.editTextJoinEmail.text.toString()
        user["Point"] = 0
        user["MonthlyEat"] = 0

        auth?.createUserWithEmailAndPassword(binding.editTextJoinEmail.text.toString(), binding.editTextJoinPw.text.toString())
            ?.addOnCompleteListener {
                task ->
                if(task.isSuccessful){
                    db.collection("Users").document(binding.editTextJoinEmail.text.toString())
                        .set(user)
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully written!")
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                } else{
                    Toast.makeText(this,"회원 가입 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

}