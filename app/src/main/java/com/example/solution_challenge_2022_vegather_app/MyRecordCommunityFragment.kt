package com.example.solution_challenge_2022_vegather_app

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.FragmentMyRecordBasicBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyRecordCommunityFragment(category : String) : Fragment() {
    private var binding : FragmentMyRecordBasicBinding? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var user : FirebaseUser
    private val recipeInfo = ArrayList<RecipeInformation>()
    private lateinit var currentUserRef : DocumentReference
    private lateinit var myRecordActivity : MyRecordActivity
    private val ct = category

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance() //현재 로그인한 사용자 가져오기
        user = auth.currentUser!!
        currentUserRef = db.collection("Users").document(user.email.toString())
        myRecordActivity = context as MyRecordActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyRecordBasicBinding.inflate(inflater,container,false)

        currentUserRef.collection("History")
            .document(ct) //내 좋아요 or Comment or Posting 목록 가져오기
            .get()
            .addOnSuccessListener {
                var HistoryList = when(ct){
                    "Like" -> it.toObject(HistoryLikedRecipe::class.java)
                    "Comment" -> it.toObject(HistoryCommentRecipe::class.java)
                    else -> null //posting 관련 data class 넣기
                }
                Log.d("like List ====>", HistoryList.toString())

                db.collection("Post") //커뮤니티 포스팅팅 목록가져오기
                    .get()
                    .addOnSuccessListener { posts->
                        var postList = posts.documents
                        var HistoryPost = posts.documents

                        for(r in postList){
                            when (HistoryList) {
                                is HistoryLikedRecipe -> if(!HistoryList?.communityRecipe?.contains(r.id)) HistoryPost.remove(r)
                                is HistoryCommentRecipe -> if(!HistoryList?.communityComment?.contains(r.id)) HistoryPost.remove(r)
                                else null //posting 관련 코드
                            }
                        }
                        val likedPostList = mutableListOf<Post>()
                            for(document in HistoryPost){
                                val title = document.get("title")
                                val subtitle = document.get("subtitle")
                                val date = document.get("timestamp")
                                val nickname = document.get("writer")
                                val uid = document.get("uid")
                                val post = Post(title=title, subtitle=subtitle, timestamp = date, writer = nickname.toString(), uid = uid.toString())
                                likedPostList.add(post)
                            }
                        val adapter = communityRecyclerAdapter(likedPostList)
                        binding!!.recyclerView.layoutManager = LinearLayoutManager(this.context)
                        binding!!.recyclerView.adapter = adapter
                    }
            }
        return binding!!.root
    }
}