package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
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

    private lateinit var documentId : String
    private lateinit var currentUserId : String
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
        binding.like.text = replyCommentList[position].like.size.toString()

        // 좋아요를 눌렀던 댓글이면 좋아요 했다는 표시 ( 좋아요 버튼이 빨간색 ) 가 있어야 한다.
        if( isLikeThisComment( replyCommentList[position].like,position) ){
            binding.likeImageView.setColorFilter(Color.parseColor("#E16D64"))
        }

        binding.likeImageView.setOnClickListener {
            if ( isLikeThisComment(replyCommentList[position].like,position) ){
                replyCommentList[position].like.remove(currentUserId)
                updateLike(replyCommentList[position].like,position)
                binding.likeImageView.setColorFilter(Color.parseColor("#BCBCBC"))
            }
            else{
                replyCommentList[position].like[currentUserId] = true
                updateLike(replyCommentList[position].like,position)
                binding.likeImageView.setColorFilter(Color.parseColor("#E16D64"))
            }
        }

        // 실시간 데이터 값 업데이트
        if( replyCommentList[position].documentId != null){
            db.collection("Recipe").document(recipeName).collection("Comment")
                .document(documentId)
                .collection("Reply")
                .document(replyCommentList[position].documentId.toString())
                .addSnapshotListener { value, error ->
                    val convertedData = value?.toObject(CommentForm::class.java)
                    binding.like.text = convertedData?.like?.size.toString()
                }
        }
    }

    override fun getItemCount(): Int {
        return replyCommentList.size
    }

    fun setData(list : ArrayList<CommentForm>, recipe : String, userId : String, docId : String){
        replyCommentList = list
        recipeName = recipe
        currentUserId = userId
        documentId = docId
    }

    fun addReplyComment( data : CommentForm){
        replyCommentList.add(data)
    }

    private fun isLikeThisComment( likedList : HashMap<String,Boolean>,position : Int) : Boolean{
        return likedList[currentUserId] == true
    }

    @SuppressLint("SetTextI18n")
    private fun updateLike(likedList : HashMap<String,Boolean>, position: Int ){
        db.collection("Recipe").document(recipeName)
            .collection("Comment")
            .document(documentId)
            .collection("Reply")
            .document(replyCommentList[position].documentId.toString())
            .update("like",likedList)
    }

}