package com.example.solution_challenge_2022_vegather_app

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommunitySearchResultBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommunityRecyclerBinding
import com.google.firebase.firestore.FirebaseFirestore

class CommunitySearchResultActivity : AppCompatActivity() {
    val binding by lazy{ActivityCommunitySearchResultBinding.inflate(layoutInflater)}
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerAdapter: communitySearchResultRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val uiBarCustom = UiBar(window)
        uiBarCustom.setStatusBarIconColor(isBlack = true)
        uiBarCustom.setNaviBarIconColor(isBlack = true)

        binding.searchBar.setOnClickListener {
            finish()
        }

        val search = intent.getStringExtra("search")
        binding.searchBar.text = search

        db = FirebaseFirestore.getInstance()

        val postList = mutableListOf<Post>()
        db.collection("Post").whereIn("title", listOf(search)).get()
            .addOnSuccessListener { result ->
                Log.d("find in db", result.toString())
                for (document in result) {
                    val title = document.get("title")
                    val subtitle = document.get("subtitle")
                    val date = document.get("timestamp")
                    val nickname = document.get("writer")
                    val uid = document.get("uid")
                    val like = document.get("like") as Long
                    val comment = document.get("comment") as Long
                    Log.d("load Post", title.toString() + " " + subtitle.toString() + " " + date.toString())
                    val post = Post(
                        title = title,
                        subtitle = subtitle,
                        timestamp = date,
                        writer = nickname.toString(),
                        uid = uid.toString(),
                        like = like.toIntOrNull(),
                        comment = comment.toIntOrNull()
                    )
                    postList.add(post)
                }
                recyclerAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "No result found", Toast.LENGTH_SHORT).show()
            }

        recyclerAdapter = communitySearchResultRecyclerAdapter(postList)
        binding.resultRecycler.adapter = recyclerAdapter
        binding.resultRecycler.layoutManager = LinearLayoutManager(this)






    }

    private fun Long.toIntOrNull(): Int? {
        val i = this.toInt()
        return if (i.toLong() == this) i else null
    }
}

class communitySearchResultRecyclerAdapter(val postData:MutableList<Post>) : RecyclerView.Adapter<communitySearchResultRecyclerAdapter.Holder>() {

    class Holder(val binding: CommunityRecyclerBinding): RecyclerView.ViewHolder(binding.root){

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
            intent.putExtra("nickname", post.writer.toString())
            intent.putExtra("document name", "${post.uid!!.chunked(10)[0]} ${post.timestamp}")
            ContextCompat.startActivity(holder.itemView?.context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return postData.size
    }
}