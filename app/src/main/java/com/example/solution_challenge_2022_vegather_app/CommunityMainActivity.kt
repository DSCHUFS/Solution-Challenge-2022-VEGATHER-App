package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatDrawableManager.preload
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommunityMainBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommunityRecyclerBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.withContext
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

        db = FirebaseFirestore.getInstance()

        val postList = mutableListOf<Post>()

        db.collection("Post")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for(document in result){
                    val title = document.get("title")
                    val subtitle = document.get("subtitle")
                    val date = document.get("timestamp")
                    val nickname = document.get("writer")
                    val uid = document.get("uid")
                    val like = document.get("like") as Long
                    val comment = document.get("comment") as Long
                    val havePhoto = document.get("havePhoto") as MutableList<String>
                    Log.d("load Post", title.toString() +" "+subtitle.toString() + " " + date.toString())
                    val post = Post(title=title, subtitle=subtitle, timestamp = date, writer = nickname.toString(), uid = uid.toString(),
                        like = like.toIntOrNull(), comment = comment.toIntOrNull(), havePhoto = havePhoto)
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


        recyclerAdapter = communityRecyclerAdapter(postList,this)
        recyclerAdapter.notifyDataSetChanged()
        binding.communityRecyclerView.adapter = recyclerAdapter
        binding.communityRecyclerView.layoutManager = LinearLayoutManager(this)


        binding.btnWrite.setOnClickListener{
            val intent = Intent(this, CommunityWriteActivity::class.java)
            startActivity(intent)
        }

        binding.btnGoMain.setOnClickListener{
            finish()
        }

        binding.searchBar.setOnClickListener {
            val searchIntent = Intent(this, CommunitySearchActivity::class.java)
            startActivity(searchIntent)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Glide.get(this).clearMemory()
    }

    private fun Long.toIntOrNull(): Int? {
        val i = this.toInt()
        return if (i.toLong() == this) i else null
    }
}

class communityRecyclerAdapter(private val postData:MutableList<Post>, private val activity: AppCompatActivity) :RecyclerView.Adapter<communityRecyclerAdapter.Holder>() {

    class Holder(val binding:CommunityRecyclerBinding):RecyclerView.ViewHolder(binding.root){

        @RequiresApi(Build.VERSION_CODES.N)
        fun set(post:Post,activity: AppCompatActivity) {
            with(binding){
                textView10.text = "${post.title}"
                textView11.text = "${post.subtitle}"
                textView12.text = "${post.timestamp.toString().substring(0, 10)}"
                textView13.text = "${post.like}"
                textView14.text = "${post.comment}"
                imageViewMainPhoto.visibility = View.INVISIBLE

                binding.imageViewMainPhoto.clipToOutline = true
                //레시피 마지막에 넣은 사진을 메인 사진으로 한다
                val lastPhoto = post.havePhoto.lastIndexOf("true")
                if(lastPhoto > 0){
                    val mainPhotoPath = "${post.uid!!.chunked(10)[0]} ${post.timestamp} $lastPhoto"
                    val storagePath = Firebase.storage.reference.child(mainPhotoPath)
                    storagePath.downloadUrl.addOnCompleteListener{
                        if (it.isSuccessful){
                            binding.imageViewMainPhoto.visibility = View.VISIBLE

                            if(!activity.isDestroyed){
                                Glide.with(this.imageViewMainPhoto)
                                    .load(it.result)
                                    .override(150,150)
                                    .into(binding.imageViewMainPhoto)
                            }

                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = CommunityRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: Holder, position: Int) {

        val post = postData[position]
        holder.set(post,activity)

//        val lastPhoto = post.havePhoto.lastIndexOf("true")
//        if(lastPhoto > 0){
//            val mainPhotoPath = "${post.uid!!.chunked(10)[0]} ${post.timestamp} $lastPhoto"
//            val storagePath = Firebase.storage.reference.child(mainPhotoPath)
//            storagePath.downloadUrl.addOnCompleteListener{
//                if (it.isSuccessful){
//                    Glide.with(holder.binding.imageViewMainPhoto).load(it.result)
//                        .into(holder.binding.imageViewMainPhoto)
//                }
//                holder.binding.imageViewMainPhoto.visibility = View.VISIBLE
//            }
//        }
//        else{
//            holder.binding.imageViewMainPhoto.visibility = View.INVISIBLE
//        }
//
        if (position <= postData.size && !activity.isDestroyed ) {
            val endPosition = if (position + 6 > postData.size) {
                postData.size
            } else {
                position + 6
            }
            postData.subList(position, endPosition ).map { it.uid!!.chunked(10)[0] }.forEach {
                preload(activity,it)
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, CommunityDetailActivity::class.java)
            intent.putExtra("post info", "${post.timestamp} ${post.title}")
            intent.putExtra("like", post.like)
            intent.putExtra("comment", post.comment)
            intent.putExtra("nickname", post.writer.toString())
            intent.putExtra("document name", "${post.uid!!.chunked(10)[0]} ${post.timestamp}")
            startActivity(holder.itemView?.context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return postData.size
    }

    private fun preload(context: Context, url : String) {
        Glide.with(context).load(url)
            .placeholder(null)
            .preload(150, 150)
    }
}

