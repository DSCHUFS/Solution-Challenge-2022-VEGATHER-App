package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.CommentRecyclerBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommentReplyRecyclerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase

@SuppressLint("NotifyDataSetChanged")
class CommentReplyAdapter(private val binding : CommentReplyRecyclerBinding,
                          private val recipeName : String,
                          private val currentUserId : String,
                          private val documentId : String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser!!
    private val userRef: DocumentReference = db.collection("Users").document(user.email.toString())

    private var replyCommentList = ArrayList<CommentForm>()

    init {
        // 댓글은 작성 시간 순으로 나열한다.
        db.collection("Recipe").document(recipeName).collection("Comment")
            .document(documentId)
            .collection("Reply")
            .whereGreaterThan("timestamp","")
            .orderBy("timestamp")
            .addSnapshotListener { value, error ->
                replyCommentList.clear()
                setCommentListData(value)
                updateReplyCount(replyCommentList.size)
                notifyDataSetChanged()
            }
    }

    inner class CommentReplyViewHolder(val binding : CommentReplyRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = CommentReplyViewHolder(CommentReplyRecyclerBinding
        .inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as CommentReplyAdapter.CommentReplyViewHolder).binding

        binding.textView38.text = replyCommentList[position].nickname
        binding.textView39.text = replyCommentList[position].text
        binding.textView40.text = replyCommentList[position].timestamp?.slice(0..10)
        binding.like.text = replyCommentList[position].like.size.toString()
        binding.likeImageView.setColorFilter(setLikeBtnColor(position))
        binding.replyDeleteBtn.visibility = setUsersCommentVisibility(replyCommentList[position].useremail,position)

        binding.likeImageView.setOnClickListener {
            if ( isLikeThisComment(replyCommentList[position].like,position) ){
                replyCommentList[position].like.remove(currentUserId)
            }
            else{
                replyCommentList[position].like[currentUserId] = true
            }
            updateLike(replyCommentList[position].like,position)
        }

        binding.replyDeleteBtn.setOnClickListener {
            deleteReply(position)
        }

    }

    override fun getItemCount(): Int {
        return replyCommentList.size
    }

    // 1. 데이터 get,set 작업

    private fun setCommentListData(snapshot : QuerySnapshot? ){
        if( snapshot != null ){
            for ( document in snapshot){
                val convertedData = document.toObject(CommentForm::class.java)
                convertedData.documentId = document.id
                replyCommentList.add(convertedData)
            }
        }
    }

    private fun setLikeBtnColor( position: Int ) : Int {
        val likedUserList : HashMap<String,Boolean> =  replyCommentList[position].like

        return if ( likedUserList[currentUserId] == true ){
            Color.parseColor("#E16D64")
        } else{
            Color.parseColor("#BCBCBC")
        }
    }

    private fun setUsersCommentVisibility( id : String?, position: Int ): Int {
        return if ( id != null && id == currentUserId) View.VISIBLE
        else View.GONE
    }

    // 2. 데이터베이스 작업

    @SuppressLint("SetTextI18n")
    private fun updateLike(likedList : HashMap<String,Boolean>, position: Int ){
        db.collection("Recipe").document(recipeName)
            .collection("Comment")
            .document(documentId)
            .collection("Reply")
            .document(replyCommentList[position].documentId.toString())
            .update("like",likedList)
    }

    private fun updateReplyCount(size : Int){
        db.collection("Recipe").document(recipeName)
            .collection("Comment")
            .document(documentId)
            .update("reply", size)
    }

    private fun deleteReply(position: Int){
        val currentSize = replyCommentList.size

        db.collection("Recipe").document(recipeName)
            .collection("Comment")
            .document(documentId)
            .collection("Reply")
            .document(replyCommentList[position].documentId.toString())
            .delete()
            .addOnSuccessListener {
                updateReplyCount(currentSize-1)
                deleteUserHistoryToComment()
            }
    }

    data class HistoryComment(
        val basicComment : HashMap<String,Int> = HashMap(),
        val communityComment : HashMap<String,Int> = HashMap()
    )

    private fun setHistoryCommentData(list: HashMap<String, Int>): HashMap<String, Int> {
        // 댓글 삭제는 무조건 본인이 작성한 댓글이 1개 이상 있다는 조건 하에 이루어진다.
        list[recipeName] = list[recipeName]!! - 1
        if( list[recipeName] == 0 ) list.remove(recipeName)

        return list
    }

    private fun deleteUserHistoryToComment() {
        userRef.collection("History").document("Comment")
            .get()
            .addOnSuccessListener {
                var container = it.toObject(HistoryComment::class.java)?.basicComment
                if (container != null) {
                    container = setHistoryCommentData(container)
                    updateUserHistoryToComment(container)
                }
            }
    }

    private fun updateUserHistoryToComment( data : HashMap<String,Int> ){
        userRef.collection("History").document("Comment")
            .update("basicComment",data)
    }

    // 3. 부가적인 작업 ( 서브 )

    private fun isLikeThisComment( likedList : HashMap<String,Boolean>,position : Int) : Boolean{
        return likedList[currentUserId] == true
    }


}