package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.SearchHistoryRecyclerBinding

class SearchHistoryAdapter(private val binding : SearchHistoryRecyclerBinding) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val dataset = ArrayList<String>()

    inner class SearchHistoryViewHolder(val binding : SearchHistoryRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = SearchHistoryViewHolder(
        SearchHistoryRecyclerBinding.inflate(
        LayoutInflater.from(parent.context),parent,false))

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as SearchHistoryAdapter.SearchHistoryViewHolder).binding
        binding.textView15.text = dataset[position]

        binding.imageButton15.setOnClickListener {
            deleteSearchHistory(position)
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun settingData(){
        for (i in 1..10){
            val s = "Masala Pasta$i"
            dataset.add(s)
        }
    }

    private fun deleteSearchHistory(position : Int){
        dataset.removeAt(position)
        if( dataset.size!=0 ){
            notifyItemRemoved(position)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteAllSearchHistory(){
        dataset.clear()
        notifyDataSetChanged()
    }
}