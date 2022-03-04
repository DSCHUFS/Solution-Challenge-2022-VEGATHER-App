package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.CommentRecyclerBinding
import com.google.firebase.firestore.FirebaseFirestore

class CommentAdapter(private val binding : CommentRecyclerBinding) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var commentObj = ArrayList<CommentForm>()
    private lateinit var recipeName : String
    private lateinit var context : Context
    private lateinit var currentUserId : String

    inner class CommentViewHolder(val binding : CommentRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = CommentViewHolder(
        CommentRecyclerBinding.inflate(
            LayoutInflater.from(parent.context),parent,false))


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as CommentAdapter.CommentViewHolder).binding

        binding.nickname.text = commentObj[position].nickname
        binding.commentText.text = commentObj[position].text
        binding.timeStamp.text = commentObj[position].timestamp
        binding.like.text = commentObj[position].like.size.toString()
        binding.reply.text = "Reply(${commentObj[position].reply})"

        if( isLikeThisComment(commentObj[position].like) ){
            binding.likeImageView.setColorFilter(Color.parseColor("#E16D64"))
        }

        binding.likeImageView.setOnClickListener {
            if ( isLikeThisComment(commentObj[position].like) ){
                commentObj[position].like.remove(currentUserId)
                updateLike(commentObj[position].like,position)
                binding.likeImageView.setColorFilter(Color.parseColor("#BCBCBC"))
            }
            else{
                commentObj[position].like[currentUserId] = true
                updateLike(commentObj[position].like,position)
                binding.likeImageView.setColorFilter(Color.parseColor("#E16D64"))
            }
        }

        binding.reply.setOnClickListener {
            loadReply(commentObj[position])
        }

        if( commentObj[position].documentId != null){
            db.collection("Recipe").document(recipeName).collection("Comment")
                .document(commentObj[position].documentId.toString())
                .addSnapshotListener { value, error ->
                    Log.d("안녕",commentObj[position].documentId.toString())
                    val convertedData = value?.toObject(CommentForm::class.java)

                    binding.like.text = convertedData?.like?.size.toString()
                    binding.reply.text = "Reply(${convertedData?.reply.toString()})"
                }
        }
    }

    override fun getItemCount(): Int {
        return commentObj.size
    }

    fun setData( data : ArrayList<CommentForm>, name : String, userId : String ){
        commentObj = data
        recipeName = name
        currentUserId = userId
    }

    fun addComment( commentInfo : CommentForm ){
        commentObj.add(commentInfo)
    }

    fun loadParentActivity( c : Context ) {
        context = c
    }

    private fun isLikeThisComment( likedList : HashMap<String,Boolean>) : Boolean{
        return likedList[currentUserId] == true
    }

    @SuppressLint("SetTextI18n")
    private fun updateLike(likedList : HashMap<String,Boolean>, position: Int ){
        db.collection("Recipe").document(recipeName)
            .collection("Comment")
            .document(commentObj[position].documentId.toString())
            .update("like",likedList)
    }

    private fun loadReply( commentInfo : CommentForm ) {
        val replyIntent = Intent(context, CommentReplyActivity::class.java)
        replyIntent.putExtra("commentInfo",commentInfo)
        replyIntent.putExtra("recipeName",recipeName)
        context.startActivity(replyIntent)
    }

}