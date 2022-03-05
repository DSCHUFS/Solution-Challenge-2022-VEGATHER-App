package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.CommentRecyclerBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommentReplyRecyclerBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class CommentReplyAdapter(private val binding : CommentReplyRecyclerBinding) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val db = FirebaseFirestore.getInstance()

    private lateinit var recipeName : String
    private var replyCommentList = ArrayList<CommentForm>()

    inner class CommentReplyViewHolder(val binding : CommentReplyRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = CommentReplyViewHolder(
        CommentReplyRecyclerBinding.inflate(
            LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as CommentReplyAdapter.CommentReplyViewHolder).binding

        binding.textView38.text = replyCommentList[position].nickname
        binding.textView39.text = replyCommentList[position].text
        binding.textView40.text = replyCommentList[position].timestamp
        binding.like.text = replyCommentList[position].like.toString()
    }

    override fun getItemCount(): Int {
        return replyCommentList.size
    }

    fun setData(list : ArrayList<CommentForm>, recipe : String){
        replyCommentList = list
        recipeName = recipe
    }

    fun addReplyComment( data : CommentForm){
        replyCommentList.add(data)
    }

}