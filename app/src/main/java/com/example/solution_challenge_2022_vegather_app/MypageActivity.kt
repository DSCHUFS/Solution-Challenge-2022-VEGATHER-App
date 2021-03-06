package com.example.solution_challenge_2022_vegather_app

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityMypageBinding
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlin.concurrent.timer

class MypageActivity : AppCompatActivity() {

    val binding by lazy { ActivityMypageBinding.inflate(layoutInflater)}
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var user : FirebaseUser
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var currentUserRef : DocumentReference
    var level = 1
    var monthlyNum = 0
    lateinit var loginWith : String

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance() //현재 로그인한 사용자 가져오기
        db = FirebaseFirestore.getInstance()
        user = auth.currentUser!!

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        currentUserRef = db.collection("Users").document(user.email.toString())
        currentUserRef.get().addOnSuccessListener {
            binding.userNickname.text = it.data?.get("NickName").toString()
            var l : Long = it.data?.get("VeganLevel") as Long
            level = l.toInt()
            calculateMonthlyVegan(level)
            with(binding){
                veganGrade.text = when(level){
                    1 -> "Freshman Vegan"
                    2 -> "Sophomore Vegan"
                    3 -> "Junior Vegan"
                    4 -> "Senior Vegan"
                    else -> "Master Vegan"
                }
            }
            loginWith = it.data?.get("LoginWith").toString()
        }

        val customUiBar = UiBar(window)
        customUiBar.setStatusBarIconColor(isBlack = false)
        customUiBar.setStatusBarTransparent()

        binding.attendanceNum.text = '+' + MyApplication.prefs.getIntPrefs("attend", 1).toString()
        textHighlightingDailyMission(binding.checkAttendance, binding.attendanceNum)

        binding.btnBack.setOnClickListener(){
            finish()
        }

        //로그아웃 버튼
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            when(loginWith){
                "facebook" -> LoginManager.getInstance().logOut()
                "google" -> googleSignInClient.signOut()
            }
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)

        }

        binding.btnLike.setOnClickListener {
            intentMyRecordActivityFrom("Like")
        }
        binding.btnComment.setOnClickListener {
            intentMyRecordActivityFrom("Comment")
        }
        binding.btnPosting.setOnClickListener {
            val intentMyRecord = Intent(this,MyPostRecordActivity::class.java)
            intentMyRecord.putExtra("category","Posting")
            startActivity(intentMyRecord)
        }

        //오늘안에 했으면 불들어오는 버튼 들
        var checkList = mutableMapOf(
            "postingCheck" to MyApplication.prefs.getPrefs("Posting","Yet"),
            "CommentCheck" to MyApplication.prefs.getPrefs("Comment", "Yet"),
            "LikeCheck" to MyApplication.prefs.getPrefs("Like", "Yet")
        )
        with(binding){
            var list1 = mutableMapOf("postingCheck" to checkPosting,"CommentCheck" to checkComment,"LikeCheck" to checkLike)
            var list2 = mutableMapOf("postingCheck" to postingNum,"CommentCheck" to commentNum,"LikeCheck" to likeNum)
            checkList.forEach{key, value ->
                if(value == "Done") {
                    list1[key]?.setBackgroundResource(R.drawable.ingredient_background_green)
                    list1[key]?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mypage_circle_green, 0, 0, 0)
                    list1[key]?.let { list2[key]?.let { it1 ->
                        textHighlightingDailyMission(it, it1)
                    } }
                }else{
                    list1[key]?.setBackgroundResource(R.drawable.comment_input_background)
                    list1[key]?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mypage_circle_gray, 0, 0, 0)
                }
            }
        }
    }
//User History DB에 저장된 데이터가 몇개인지 세서 binding 바꾸어주어야함.
    override fun onStart() {
        super.onStart()
        currentUserRef.collection("History")
            .get()
            .addOnSuccessListener {
                for (document in it){
                    when(document.id){ //user의 History DB를 가져와서 갯수를 체크한다.
                        "Comment" -> {
                            var userHDB = document.toObject(HistoryCommentRecipe::class.java)
                            binding.commentNum.text = '+' + (userHDB.basicComment.size + userHDB.communityComment.size).toString()
                        }
                        "Like" -> {
                            var userHDB = document.toObject(HistoryLikedRecipe::class.java)
                            binding.likeNum.text = '+' + (userHDB.basicRecipe.size + userHDB.communityRecipe.size).toString()
                        }
                        "Posting" -> {
                            var userHDB = document.toObject(HistoryPosting::class.java)
                            binding.postingNum.text = '+' + (userHDB.posting.size).toString()
                        }
                    }
                }
            }
    }

    private fun intentMyRecordActivityFrom(text : String){
        val intentMyRecord = Intent(this,MyRecordActivity::class.java)
        intentMyRecord.putExtra("category",text)
        startActivity(intentMyRecord)
    }

    private fun textHighlightingDailyMission(dailyButton : Button, pointNumber : TextView){
        dailyButton.setTextColor(Color.parseColor("#81E768"))
        pointNumber.setTextColor(Color.parseColor("#81E768"))
    }

    private fun calculateMonthlyVegan(level : Int) {
        var baseG = when (level) {
            1 -> 1000
            2 -> 800
            3 -> 640
            else -> 512
        }
        val attendanceGrade = 0.1 * MyApplication.prefs.getIntPrefs("attendNum", 1)
        val postingGrade = 0.4 * MyApplication.prefs.getIntPrefs("postingNum", 0)
        val commentGrade = 0.25 * MyApplication.prefs.getIntPrefs("commentNum", 0)
        val likeGrade = 0.25 * MyApplication.prefs.getIntPrefs("likeNum", 0)

        val gradeLists = listOf(attendanceGrade, postingGrade, commentGrade, likeGrade)

        for (grade in gradeLists) {
            val nowG = grade * baseG

            monthlyNum += nowG.toInt()
            if (monthlyNum >= 1000) {
                //Level upgrade 코드
                val levelUp : Int = monthlyNum / 1000
                currentUserRef.update("VeganLevel", FieldValue.increment(levelUp.toLong()))

                monthlyNum %= 1000
                baseG = (baseG * 0.8).toInt()
                //lv up 했으니, 이전 기록들은 다 0으로 갱신
                MyApplication.prefs.setIntPrefs("attendNum", 0)
                MyApplication.prefs.setIntPrefs("postingNum", 0)
                MyApplication.prefs.setIntPrefs("commentNum", 0)
                MyApplication.prefs.setIntPrefs("likeNum", 0)
            }
            binding.monthlyNum.text = monthlyNum.toString()
            binding.monthlyPercent.text = (monthlyNum / 10).toString() + '%'
        }
        currentUserRef.get()
            .addOnSuccessListener {
                var l: Long = it.data?.get("VeganLevel") as Long
                binding.btnLevel.text = "LV " + l.toString()
            }

        //그래프 그리기
        if(monthlyNum != 0) {
            var i: Int = 0
            timer(period = 2, initialDelay = 500) {
                i++
                binding.circleBar.setProgress(i.toFloat())
                if (i == (monthlyNum * 360 / 1000)) {
                    cancel()
                }
            }
        }
    }
}