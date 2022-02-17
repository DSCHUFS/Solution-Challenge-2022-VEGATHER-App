package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.CommentRecyclerBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommentReplyRecyclerBinding

class CommentReplyAdapter(private val binding : CommentReplyRecyclerBinding) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val dataset = ArrayList<String>()

    inner class CommentReplyViewHolder(val binding : CommentReplyRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = CommentReplyViewHolder(
        CommentReplyRecyclerBinding.inflate(
            LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as CommentReplyAdapter.CommentReplyViewHolder).binding
        binding.textView38.text = "Cristiano messi"
        binding.textView39.text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry."
        binding.textView40.text = "2022.2.13"
        binding.like.text = "999+"
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun settingData(){
        for (i in 1..15){
            dataset.add((i+100).toString())
        }
    }

}