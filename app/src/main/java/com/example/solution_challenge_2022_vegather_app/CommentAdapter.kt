package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.CommentRecyclerBinding

class CommentAdapter(private val binding : CommentRecyclerBinding) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    val dataset = ArrayList<String>()
    private lateinit var context : Context

    inner class CommentViewHolder(val binding : CommentRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = CommentViewHolder(
        CommentRecyclerBinding.inflate(
            LayoutInflater.from(parent.context),parent,false))


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as CommentAdapter.CommentViewHolder).binding
        binding.textView38.text = "Masala Pasta"
        binding.textView39.text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry."
        binding.textView40.text = "2522.11.21"
        binding.like.text = "999+"
        binding.textView42.text = "Reply(" + dataset[position] + ")"

        binding.textView42.setOnClickListener {
            val replyIntent = Intent(context,CommentReplyActivity::class.java)
            replyIntent.putExtra("nickname",binding.textView38.text)
            context.startActivity(replyIntent)
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun settingData(){
        for (i in 1..15){
            dataset.add((i+100).toString())
        }
    }

    fun loadParentActivity( c : Context ) {
        context = c
    }

}