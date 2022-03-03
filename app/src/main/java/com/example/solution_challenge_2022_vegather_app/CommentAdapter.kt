package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
        binding.like.text = commentObj[position].like.toString()
        binding.reply.text = "Reply(${commentObj[position].reply})"

        binding.reply.setOnClickListener {
            loadReply(commentObj[position])
        }

        db.collection("Recipe").document(recipeName).collection("Comment")
            .document(commentObj[position].documentId.toString())
            .addSnapshotListener { value, error ->
                Log.d("안녕",commentObj[position].documentId.toString())
                val convertedData = value?.toObject(CommentForm::class.java)
                binding.like.text = convertedData?.like.toString()
                binding.reply.text = "Reply(${convertedData?.reply.toString()})"
            }
    }

    override fun getItemCount(): Int {
        return commentObj.size
    }

    fun setData( data : ArrayList<CommentForm>, name : String ){
        commentObj = data
        recipeName = name
    }

    fun addComment( commentInfo : CommentForm ){
        commentObj.add(commentInfo)
    }

    fun loadParentActivity( c : Context ) {
        context = c
    }

    @SuppressLint("SetTextI18n")
    private fun onDataChangedListener(position : Int){
        db.collection("Recipe").document(recipeName).collection("Comment")
            .document(commentObj[position].documentId.toString())
            .addSnapshotListener { value, error ->
                Log.d("안녕",commentObj[position].documentId.toString())
                val convertedData = value?.toObject(CommentForm::class.java)
                binding.like.text = convertedData?.like.toString()
                binding.reply.text = "Reply(${convertedData?.reply.toString()})"
            }
    }

    private fun loadReply( commentInfo : CommentForm ) {
        val replyIntent = Intent(context, CommentReplyActivity::class.java)
        replyIntent.putExtra("commentInfo",commentInfo)
        replyIntent.putExtra("recipeName",recipeName)
        context.startActivity(replyIntent)
    }

}