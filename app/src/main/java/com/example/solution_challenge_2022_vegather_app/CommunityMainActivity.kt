package com.example.solution_challenge_2022_vegather_app

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommunityMainBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommunityRecyclerBinding

class CommunityMainActivity : AppCompatActivity() {

    val binding by lazy{ActivityCommunityMainBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val post = loadPost()
        val recyclerAdapter = RecyclerAdapter(post)
        binding.communityRecyclerView.adapter = recyclerAdapter
        binding.communityRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.btnWrite.setOnClickListener{
            val intent = Intent(this, CommunityWriteActivity::class.java)
            startActivity(intent)
        }

        binding.btnGoMain.setOnClickListener{
            finish()
        }

    }

    fun loadPost() : MutableList<Post> {
        val postList = mutableListOf<Post>()
        for (idx in 1..10){
            val title = "Title $idx"
            val subtitle = "Lorem Ipsum is simply dummy text printing and typesetting industry. Lorem Ipsum is simply dummy text printing and typesetting industry."
            val date = System.currentTimeMillis()
            val post = Post(title, subtitle, date)
            postList.add(post)
        }
        return postList
    }



}

class RecyclerAdapter(val postData:MutableList<Post>) :RecyclerView.Adapter<RecyclerAdapter.Holder>() {

    class Holder(val binding:CommunityRecyclerBinding):RecyclerView.ViewHolder(binding.root){

        @RequiresApi(Build.VERSION_CODES.N)
        fun set(post:Post) {
            with(binding){
                textView10.text = "${post.title}"
                textView11.text = "${post.subtitle}"
                val sdf = SimpleDateFormat("MM.dd")
                val formattedDate = sdf.format(post.timestamp)
                textView12.text = formattedDate
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
    }

    override fun getItemCount(): Int {
        return postData.size
    }
}

