package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.format.DateFormat
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.SearchAutocompleteRecyclerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AutocompleteSearchAdapter(private val binding : SearchAutocompleteRecyclerBinding,private val listener: SelectedSearchHistoryListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser!!
    private val userRef: DocumentReference = db.collection("Users").document(user.email.toString())

    private lateinit var context : Context
    private lateinit var foodName : ArrayList<RecipeInformation>
    private lateinit var startIndex : ArrayList<Int>
    private var length = 1

    inner class AutocompleteSearchViewHolder(val binding : SearchAutocompleteRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = AutocompleteSearchViewHolder(
        SearchAutocompleteRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as AutocompleteSearchViewHolder).binding
        // 텍스트 하이라이팅으로 입력값에 해당하는 부분을 강조시켜 연관성을 드러낸다.
        textHighlighting(binding, position)

        binding.relatedText.setOnClickListener {
            addKeywordToSearchHistory(binding.relatedText.text.toString())
            listener.onSearchHistorySelected(binding.relatedText.text.toString())
        }
    }

    override fun getItemCount(): Int {
        return foodName.size
    }

    fun setData( foodNameList : ArrayList<RecipeInformation>, Index : ArrayList<Int>,len : Int ){
        foodName = foodNameList
        startIndex = Index
        length = len
    }

    fun loadParentActivity(c : Context){
        context = c
    }

    private fun textHighlighting(binding : SearchAutocompleteRecyclerBinding,position : Int){
        val builder = SpannableStringBuilder(foodName[position].name)
        builder.setSpan(ForegroundColorSpan(Color.parseColor("#81E678")),
            startIndex[position],
            startIndex[position] + length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.relatedText.text = builder
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTime(): String {
        val now = System.currentTimeMillis()
        return DateFormat.format("yyyy.MM.dd kk:mm:ss",now).toString()
    }

    data class SearchHistory(
        val basicSearch : HashMap<String,String> = HashMap(),
        val communitySearch : HashMap<String,String> = HashMap()
    )

    private fun updateSearchHistory( updatedData : HashMap<String,String>){
        userRef.collection("History").document("Search")
            .update("basicSearch",updatedData)
    }

    private fun addKeywordToSearchHistory(text : String){
        userRef.collection("History").document("Search").get()
            .addOnSuccessListener {
                val searchHistoryHash = it.toObject(SearchHistory::class.java)?.basicSearch
                searchHistoryHash?.set(text, getCurrentTime())
                if (searchHistoryHash != null) {
                    updateSearchHistory(searchHistoryHash)
                }
            }
    }
}