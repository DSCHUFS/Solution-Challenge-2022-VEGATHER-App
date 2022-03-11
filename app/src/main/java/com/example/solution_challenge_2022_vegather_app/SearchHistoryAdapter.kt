package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.SearchHistoryRecyclerBinding
import com.facebook.gamingservices.cloudgaming.CloudGameLoginHandler.init
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

@SuppressLint("NotifyDataSetChanged")
class SearchHistoryAdapter(private val binding : SearchHistoryRecyclerBinding,
                           private val listener : SelectedSearchHistoryListener,
                           private val context : Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){


    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser!!
    private val userRef: DocumentReference = db.collection("Users").document(user.email.toString())

    private lateinit var searchHistoryHash : HashMap<String,String>
    private val searchHistoryList = ArrayList<String>()
    init {
        userRef.collection("History").document("Search")
            .addSnapshotListener { value, error ->
                searchHistoryList.clear()
                setCommentListAndHash(value)
                notifyDataSetChanged()
            }
    }

    inner class SearchHistoryViewHolder(val binding : SearchHistoryRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = SearchHistoryViewHolder(SearchHistoryRecyclerBinding
        .inflate(LayoutInflater.from(parent.context),parent,false))

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as SearchHistoryAdapter.SearchHistoryViewHolder).binding

        binding.searchHistoryText.text = searchHistoryList[position]

        binding.searchHistoryText.setOnClickListener {
            listener.onSearchHistorySelected(binding.searchHistoryText.text.toString())
        }

        binding.imageButton15.setOnClickListener {
            deleteOneSearchHistory(position)
        }
    }

    override fun getItemCount(): Int {
        return searchHistoryList.size
    }

    data class SearchHistory(
        val basicSearch : HashMap<String,String> = HashMap(),
        val communitySearch : HashMap<String,String> = HashMap()
    )

    private fun setCommentListAndHash(snapshot: DocumentSnapshot?){
        if( snapshot != null ){
            searchHistoryHash = snapshot.toObject(SearchHistory::class.java)?.basicSearch!!
            val sortedHistoryByTimestamp = searchHistoryHash.toList()
                .sortedWith(compareByDescending { it.second })
                .toMap()

            for( element in sortedHistoryByTimestamp ) searchHistoryList.add(element.key)
        }
    }

    private fun updateSearchHistory( searchHistoryHash : HashMap<String,String>){
        userRef.collection("History").document("Search")
            .update("basicSearch",searchHistoryHash)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteOneSearchHistory(position : Int){
        searchHistoryHash.remove(searchHistoryList[position])
        updateSearchHistory(searchHistoryHash)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteAllSearchHistory(){
        searchHistoryHash.clear()
        updateSearchHistory(searchHistoryHash)
    }
}