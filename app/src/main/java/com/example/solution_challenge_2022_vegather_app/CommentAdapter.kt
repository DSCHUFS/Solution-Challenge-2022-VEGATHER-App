package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.CommentRecyclerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

@SuppressLint("NotifyDataSetChanged")
class CommentAdapter(private val binding : CommentRecyclerBinding,
                     private val recipeName: String,
                     private val context: Context,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser!!
    private val userRef: DocumentReference = db.collection("Users").document(user.email.toString())

    private var commentObj = ArrayList<CommentForm>()
    private lateinit var currentUserId : String

    init {
        // 댓글은 작성 시간 순으로 나열한다.
        db.collection("Recipe").document(recipeName).collection("Comment")
            .whereGreaterThan("timestamp","")
            .orderBy("timestamp")
            .addSnapshotListener { value, error ->
                commentObj.clear()
                setCommentListData(value)
                updateCommentCount(commentObj.size)
                notifyDataSetChanged()
            }
    }

    inner class CommentViewHolder(val binding : CommentRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = CommentViewHolder(
        CommentRecyclerBinding.inflate(
            LayoutInflater.from(parent.context),parent,false))


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as CommentAdapter.CommentViewHolder).binding

        Log.d("코멘트 어답터 뷰홀더 시작", "현재 position : $position")

        binding.nickname.text = commentObj[position].nickname
        binding.commentText.text = commentObj[position].text
        binding.timeStamp.text = commentObj[position].timestamp?.slice(0..10)
        binding.like.text = commentObj[position].like.size.toString()
        binding.reply.text = "Reply(${commentObj[position].reply})"
        binding.commentDeleteBtn.visibility = setUsersCommentVisibility(commentObj[position].useremail,position)
        binding.likeImageView.setColorFilter(setLikeBtnColor(position))

        binding.likeImageView.setOnClickListener {
            if ( isLikeThisComment(commentObj[position].like) ){
                commentObj[position].like.remove(currentUserId)
            }
            else{
                commentObj[position].like[currentUserId] = true
            }
            updateLike(commentObj[position].like,position)
        }

        binding.commentDeleteBtn.setOnClickListener {
           deleteComment(position)
        }

        binding.reply.setOnClickListener {
            loadReply(commentObj[position])
        }
    }

    override fun getItemCount(): Int {
        return commentObj.size
    }

    // 1. 데이터 get,set 작업

    private fun setCommentListData(snapshot : QuerySnapshot? ){
        if( snapshot != null ){
            for ( document in snapshot){
                val convertedData = document.toObject(CommentForm::class.java)
                convertedData.documentId = document.id
                commentObj.add(convertedData)
            }
        }
    }

    fun setCurrentUserId( userId : String){
        currentUserId = userId
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteData(position: Int){
        commentObj.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position,itemCount)
        Log.d("코멘트 어답터 데이터 삭제를 알림",commentObj.toString())
    }

    private fun setUsersCommentVisibility( id : String?, position: Int ): Int {
        return if ( id != null && id == currentUserId) View.VISIBLE
        else View.GONE
    }

    private fun setLikeBtnColor( position: Int ) : Int {
        val likedUserList : HashMap<String,Boolean> =  commentObj[position].like

        return if ( likedUserList[currentUserId] == true ){
            Color.parseColor("#E16D64")
        } else{
            Color.parseColor("#BCBCBC")
        }
    }

    // 2. 데이터베이스 관련 작업들 ( 댓글, 좋아요 )

    private fun deleteComment(position: Int){
        val currentSize = commentObj.size

        db.collection("Recipe").document(recipeName)
            .collection("Comment")
            .document(commentObj[position].documentId.toString())
            .delete()
            .addOnSuccessListener {
                updateCommentCount(currentSize-1)
                deleteUserHistoryToComment()
                Log.d("코멘트 어답터 데이터 베이스에 코멘트 삭제 완료","완료 position : $position")
            }
    }

    private fun updateCommentCount(size : Int){
        db.collection("Recipe").document(recipeName)
            .update("comment",size)
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

    data class HistoryComment(
        val basicComment : HashMap<String,Int> = HashMap(),
        val communityComment : HashMap<String,Int> = HashMap()
    )

    private fun updateUserHistoryToComment( data : HashMap<String,Int> ){
        userRef.collection("History").document("Comment")
            .update("basicComment",data)
    }

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

    // 3. 부가적인 작업 ( 서브 )

    private fun loadReply( commentInfo : CommentForm ) {
        val replyIntent = Intent(context, CommentReplyActivity::class.java)
        replyIntent.putExtra("commentInfo",commentInfo)
        replyIntent.putExtra("recipeName",recipeName)
        context.startActivity(replyIntent)
    }


}