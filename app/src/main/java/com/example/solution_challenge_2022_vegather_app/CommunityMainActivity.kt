package com.example.solution_challenge_2022_vegather_app

import android.content.ContentValues.TAG
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommunityMainBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommunityRecyclerBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.storage.ktx.storage
import java.util.ArrayList

class CommunityMainActivity : AppCompatActivity() {

    val binding by lazy{ActivityCommunityMainBinding.inflate(layoutInflater)}
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerAdapter: communityRecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val uiBarCustom = UiBar(window)
        uiBarCustom.setStatusBarIconColor(isBlack = true)
        uiBarCustom.setNaviBarIconColor(isBlack = true)

        db = FirebaseFirestore.getInstance()
//        val post = loadPost()

        //var recyclerAdapter = RecyclerAdapter(post)
        val postList = mutableListOf<Post>()
        db.collection("Post")
            .get()
            .addOnSuccessListener { result ->
                for(document in result){
                    val title = document.get("title")
                    val subtitle = document.get("subtitle")
                    val date = document.get("timestamp")
                    Log.d("load Post", title.toString() +" "+subtitle.toString() + " " + date.toString())
                    val post = Post(title=title, subtitle=subtitle, timestamp = date)
                    postList.add(post)
                    Log.d("add post to postList", postList[postList.size-1].title.toString() + postList[postList.size-1].subtitle.toString() + postList[postList.size-1].timestamp.toString())
                    Log.d("before iter end post list", postList.toString())
                }
//                recyclerAdapter = RecyclerAdapter(postList)
                recyclerAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e->
                Log.d(TAG, "Error getting documents: ", e)
            }

        recyclerAdapter = communityRecyclerAdapter(postList)
        recyclerAdapter.notifyDataSetChanged()
        binding.communityRecyclerView.adapter = recyclerAdapter
        binding.communityRecyclerView.layoutManager = LinearLayoutManager(this)


        binding.btnWrite.setOnClickListener{
            val intent = Intent(this, CommunityWriteActivity::class.java)
            startActivity(intent)
        }

        binding.btnGoMain.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }


//    fun loadPost() : MutableList<Post> {
//        val postList = mutableListOf<Post>()
//        db.collection("Post")
//            .get()
//            .addOnSuccessListener { result ->
//                for(document in result){
//                    val title = document.get("title")
//                    val subtitle = document.get("subtitle")
//                    val date = document.get("timestamp")
//                    Log.d("load Post", title.toString() + subtitle.toString() + date.toString())
//                    val post = Post(title=title, subtitle=subtitle, timestamp = date)
//                    postList.add(post)
//                    Log.d("add post to postList", postList[postList.size-1].title.toString() + postList[postList.size-1].subtitle.toString() + postList[postList.size-1].timestamp.toString())
//                    Log.d("before iter end post list", postList.toString())
//                }
//                recyclerAdapter = RecyclerAdapter(postList)
//                recyclerAdapter.notifyDataSetChanged()
//            }
//            .addOnFailureListener { e->
//                Log.d(TAG, "Error getting documents: ", e)
//            }
//        //        for (idx in 1..10){
////            val title = "Title $idx"
////            val subtitle = "Lorem Ipsum is simply dummy text printing and typesetting industry. Lorem Ipsum is simply dummy text printing and typesetting industry."
////            val date = System.currentTimeMillis()
////            val post = Post(title=title, subtitle=subtitle, timestamp = date.toString())
////            postList.add(post)
////        }
////        Thread.sleep(1000)
//
//        Log.d("after iter end post list", postList.toString())
//        return postList
//    }
}

class communityRecyclerAdapter(val postData:MutableList<Post>) :RecyclerView.Adapter<communityRecyclerAdapter.Holder>() {

    class Holder(val binding:CommunityRecyclerBinding):RecyclerView.ViewHolder(binding.root){

        @RequiresApi(Build.VERSION_CODES.N)
        fun set(post:Post) {
            with(binding){
                textView10.text = "${post.title}"
                textView11.text = "${post.subtitle}"
                textView12.text = "${post.timestamp.toString().substring(0, 10)}"
                textView13.text = "${post.like}"
                textView14.text = "${post.comment}"

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = CommunityRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val post = postData[position]
        holder.set(post)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, CommunityDetailActivity::class.java)
            intent.putExtra("post info", "${post.timestamp} ${post.title}")
            intent.putExtra("like", post.like)
            intent.putExtra("comment", post.comment)
            startActivity(holder.itemView?.context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return postData.size
    }
}

