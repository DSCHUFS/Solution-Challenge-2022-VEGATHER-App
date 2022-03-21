package com.example.solution_challenge_2022_vegather_app

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityMyPostRecordBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyPostRecordActivity : AppCompatActivity() {
    val binding by lazy{ActivityMyPostRecordBinding.inflate(layoutInflater)}
    private lateinit var db: FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var user : FirebaseUser
    private lateinit var currentUserRef : DocumentReference
    private lateinit var recyclerAdapter: communityRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val intent = intent
        val title = intent.getStringExtra("category").toString()
        binding.textView59.text = title

        val uiBarCustom = UiBar(window)
        uiBarCustom.setStatusBarIconColor(isBlack = true)

        binding.imageButton9.setOnClickListener {
            finish()
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance() //현재 로그인한 사용자 가져오기
        user = auth.currentUser!!
        currentUserRef = db.collection("Users").document(user.email.toString())

        currentUserRef.collection("History")
            .document(title) //내 좋아요 or Comment or Posting 목록 가져오기
            .get()
            .addOnSuccessListener {
                var HistoryList = it.toObject(HistoryPosting::class.java)
                val likedPostList = mutableListOf<Post>()
                Log.d("like List ====>", HistoryList.toString())

                db.collection("Post") //커뮤니티 포스팅팅 목록가져오기
                    .get()
                    .addOnSuccessListener { posts->
                        var postList = posts.documents
                        var HistoryPost = posts.documents

                        for(r in postList){
                            if(HistoryList?.posting?.contains(r.id) == false)
                                HistoryPost.remove(r)
                        }
                        for(document in HistoryPost){
                            val title = document.get("title")
                            val subtitle = document.get("subtitle")
                            val date = document.get("timestamp")
                            val nickname = document.get("writer")
                            val uid = document.get("uid")
                            val like = document.get("like") as Long
                            val comment = document.get("comment") as Long
                            val havePhoto = document.get("havePhoto") as MutableList<String>
                            Log.d("load Post", title.toString() +" "+subtitle.toString() + " " + date.toString())
                            val post = Post(title=title, subtitle=subtitle, timestamp = date, writer = nickname.toString(),
                                uid = uid.toString(), like = like.toIntOrNull(), comment = comment.toIntOrNull(), havePhoto = havePhoto)
                            likedPostList.add(post)
                        }
                        recyclerAdapter.notifyDataSetChanged()
                    }
                recyclerAdapter = communityRecyclerAdapter(likedPostList)
                recyclerAdapter.notifyDataSetChanged()
                binding.mypostRecycler.adapter = recyclerAdapter
                binding.mypostRecycler.layoutManager = LinearLayoutManager(this)
            }



    }
}
private fun Long.toIntOrNull(): Int? {
    val i = this.toInt()
    return if (i.toLong() == this) i else null
}