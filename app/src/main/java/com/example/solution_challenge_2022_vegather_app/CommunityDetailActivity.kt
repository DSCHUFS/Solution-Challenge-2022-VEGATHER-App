package com.example.solution_challenge_2022_vegather_app

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommunityDetailBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommunityIngredientRecyclerBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommunityOrderRecyclerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class CommunityDetailActivity : AppCompatActivity() {

    private lateinit var db : FirebaseFirestore
    private var currentStatusOfLike = false
    private lateinit var auth : FirebaseAuth
    private lateinit var user : FirebaseUser
    private lateinit var currentUserRef : DocumentReference
    private lateinit var documentName : String
    val binding by lazy{ActivityCommunityDetailBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUiBarColor()

        binding.imageButtonBack.setOnClickListener {
            finish()
        }

        binding.textNickname.text = intent.getStringExtra("nickname")
        var nowLike : Int? =  intent.getIntExtra("like", -1)
        var nowComment : Int? = intent.getIntExtra("comment", -1)
        documentName = intent.getStringExtra("document name").toString()
        Log.d("document name", documentName)

        binding.textLike.text = nowLike.toString()
        binding.textComment.text = nowComment.toString()

        db = FirebaseFirestore.getInstance()
        val postInfo = intent.getStringExtra("post info").toString().split(" ")
        val testInfo = intent.getStringExtra("post info").toString()
        Log.d("get post info", testInfo)
        binding.textTimsStamp.text = postInfo[0]
        val postTimeStamp = postInfo[0] + " " + postInfo[1]
        val postTitle = testInfo.substring(20)

        Log.d(TAG, postTimeStamp)
        Log.d(TAG, postTitle)

        val orderList = mutableListOf<Any?>()
        val havePhotoIndex = mutableListOf<Int?>()
        val uidForPhoto = mutableListOf<String?>()
        val timestampForPhoto = mutableListOf<String?>()
        val orderAdapter = OrderRecyclerAdapter(orderList, havePhotoIndex, uidForPhoto, timestampForPhoto,this)

        //재료 리사이클러뷰 왼쪽
        val ingredientNameListForEven = mutableListOf<String?>()
        val ingredientAmountListForEven = mutableListOf<String?>()
        val ingredientAdapterEven = IngredientRecyclerAdapter(ingredientNameListForEven, ingredientAmountListForEven)
        //재료 리사이클러뷰 오른쪽
        val ingredientNameListForOdd = mutableListOf<String?>()
        val ingredientAmountListForOdd = mutableListOf<String?>()
        val ingredientAdapterOdd = IngredientRecyclerAdapter(ingredientNameListForOdd, ingredientAmountListForOdd)

        //Firebase에서 해당 post찾아 필요한 data 가져오기
        db.collection("Post").whereEqualTo("timestamp", postTimeStamp)
            .whereEqualTo("title", postTitle)
            .get()
            .addOnSuccessListener { result ->
                Log.d("get from db", result.toString())
                for(document in result){
                    binding.textTitle.text = document.get("title").toString()
                    binding.textSubtitle.text = document.get("subtitle").toString()
                    val havePhoto = document.get("havePhoto") as MutableList<String>
                    Log.d("get have Photo from db", havePhoto.toString())
                    for(i in 0 until havePhoto.size){
                        if(havePhoto[i] == "true"){
                            havePhotoIndex.add(i)
                        }
                    }
                    Log.d("have Photo index", havePhotoIndex.toString())
                    Log.d(TAG, document.get("recipe").toString())
                    Log.d(TAG, document.get("recipe")!!.javaClass.toString())
                    val tempList = document.get("recipe") as MutableList<Any?>
                    Log.d(TAG, tempList.toString())
                    for(i in 0 until tempList.size){
                        orderList.add(null)
                        orderList[i] = tempList[i]
                    }
                    val getUid = document.get("uid")
                    if (Firebase.auth.currentUser!!.uid == getUid){
                        binding.imageButtonDeletePost.visibility = View.VISIBLE
                    }
                    uidForPhoto.add(getUid.toString().chunked(10)[0])
                    Log.d("uidForPhoto", uidForPhoto.toString())

                    val getTimeStamp = document.get("timestamp").toString()
                    timestampForPhoto.add(getTimeStamp)
                    Log.d("timestampForPhoto", timestampForPhoto.toString())


                    val ingredientNameListFromDB = document.get("ingredientName") as MutableList<String?>
                    val ingredientAmountListFromDB = document.get("ingredientAmount") as MutableList<String?>
                    if(ingredientNameListFromDB.size == ingredientAmountListFromDB.size){
                        for(i in 0 until ingredientNameListFromDB.size){
                            if(i % 2 == 0){
                                ingredientNameListForEven.add(ingredientNameListFromDB[i])
                                ingredientAmountListForEven.add(ingredientAmountListFromDB[i])
                            }
                            else{
                                ingredientNameListForOdd.add(ingredientNameListFromDB[i])
                                ingredientAmountListForOdd.add(ingredientAmountListFromDB[i])
                            }
                        }

                    }

                    //메인 사진 등록
                    addMainPhoto(havePhotoIndex, uidForPhoto, timestampForPhoto)
                }
                orderAdapter.notifyDataSetChanged()
                ingredientAdapterEven.notifyDataSetChanged()
                ingredientAdapterOdd.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.d(TAG,"error in get from db")
            }



        orderAdapter.notifyDataSetChanged()
        binding.orderRecycler.adapter = orderAdapter
        binding.orderRecycler.layoutManager = LinearLayoutManager(this)

        ingredientAdapterEven.notifyDataSetChanged()
        binding.ingredientRecyclerEven.adapter = ingredientAdapterEven
        binding.ingredientRecyclerEven.layoutManager = LinearLayoutManager(this)

        ingredientAdapterOdd.notifyDataSetChanged()
        binding.ingredientRecyclerOdd.adapter = ingredientAdapterOdd
        binding.ingredientRecyclerOdd.layoutManager = LinearLayoutManager(this)

        //post 삭제 기능
        binding.imageButtonDeletePost.setOnClickListener {
            deletePost(uidForPhoto, postTimeStamp, havePhotoIndex)
        }

        //좋아요 버튼 작업
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        currentUserRef = db.collection("Users").document(user.email.toString())

        currentUserRef.collection("History")
            .document("Like")
            .get()
            .addOnSuccessListener {
                currentStatusOfLike = isRecipeLiked(it, documentName)
                updateLikeButtonColor(currentStatusOfLike)
            }

        binding.imageViewLike.setOnClickListener {
            currentStatusOfLike = !currentStatusOfLike
            Log.d("click like button -> now like state", currentStatusOfLike.toString())
            updateLike(isLiked = currentStatusOfLike, nowLike)
            updateLikeButtonColor(currentStatusOfLike)
            if(currentStatusOfLike){
                MyApplication.prefs.setPrefs("Like", "Done")
                MyApplication.prefs.setIntPrefs("likeNum", MyApplication.prefs.getIntPrefs("likeNum", 0)+1)
            }
        }

        db.collection("Post").document(documentName)
            .addSnapshotListener { value, error ->
                val getLike = value!!.get("like")
                if (getLike != null){
                    Log.d("type of getLike", getLike.javaClass.name)
                    Log.d("type of getLike", (getLike as Long).toIntOrNull()!!.javaClass.name)
                    nowLike = getLike.toIntOrNull()
                    nowComment = (value.get("comment") as Long).toIntOrNull()

                    binding.textLike.text = nowLike.toString()
                    binding.textComment.text = nowComment.toString()
                }
            }

        //댓글 작업
        binding.commentContainer.setOnClickListener {
            val commentIntent = Intent(this,CommunityCommentActivity::class.java)
            commentIntent.putExtra("document name", documentName)
            startActivity(commentIntent)
        }

        val customUiBar = UiBar(window)
        binding.communityScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if( scrollY < binding.imageViewMain.height ){
                customUiBar.setStatusBarIconColor(isBlack = false)
            }
            else{
                customUiBar.setStatusBarIconColor(isBlack = true)
            }
        }
        binding.imageButtonDeletePost.setColorFilter(Color.WHITE)
    }// end of onCreate

    override fun onDestroy() {
        super.onDestroy()
        Glide.get(this).clearMemory()
    }

    private fun setUiBarColor(){
        val customUiBar = UiBar(window)
        if( Build.VERSION.SDK_INT >= 30){
            customUiBar.setStatusBarTransparent()
        }
        else if( Build.VERSION.SDK_INT >= 23){
            customUiBar.setStatusBarIconColor(isBlack = false)
        }
    }

    private fun addMainPhoto(havePhotoIndex: MutableList<Int?>, uidForPhoto: MutableList<String?>, timestampForPhoto: MutableList<String?>) {
        Log.d("fun_addmainphoto", havePhotoIndex.toString())
        if (havePhotoIndex.isNotEmpty()) {
            val lastPhotoIndex = havePhotoIndex[havePhotoIndex.size - 1]
            val mainPhotoPath = "${uidForPhoto[0]} ${timestampForPhoto[0]} $lastPhotoIndex"
            val storagePath = Firebase.storage.reference.child(mainPhotoPath)
            Log.d("detail_mainPhotoPath", mainPhotoPath)

            if( !this.isDestroyed ){
                Glide.with(this)
                    .load(R.drawable.loading_bigsize_dark)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerInside()
                    .into(binding.imageViewMain)

                storagePath.downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Glide.with(this)
                            .load(it.result)
                            .centerCrop()
                            .into(binding.imageViewMain)
                    }
                }
            }
        }
    }

    //Long to Int type casting
    private fun Long.toIntOrNull(): Int? {
        val i = this.toInt()
        return if (i.toLong() == this) i else null
    }

    private fun isRecipeLiked(likedRecipe: DocumentSnapshot?, documentName: String): Boolean {
        val likedList = likedRecipe!!.toObject(HistoryLikedRecipe::class.java)
        return likedList?.communityRecipe?.contains(documentName) ?: false
    }

    private fun updateLikeButtonColor(isLiked: Boolean) {
        when(isLiked){
            true -> binding.imageViewLike
                .setColorFilter(Color.parseColor("#81E678"))
            false -> binding.imageViewLike
                .setColorFilter(Color.parseColor("#BCBCBC"))
        }
    }

    private fun updateLike(isLiked: Boolean, nowLike: Int?) {
        val addedNum = if (isLiked) 1 else -1

        if(nowLike is Int){
            db.collection("Post")
                .document(documentName)
                .update("like", nowLike + addedNum)
                .addOnSuccessListener {
                    updateLikedRecipeInDataBase(isLiked)
                }
        }
        else{
            Log.d(TAG, "nowLike is null")
        }

    }

    private fun updateLikedRecipeInDataBase(isLiked: Boolean) {
        when(isLiked){
            true -> {
                currentUserRef.collection("History").document("Like")
                    .update("communityRecipe", FieldValue.arrayUnion(documentName))
                    .addOnSuccessListener {
                        Log.d("addLikedRecipeInBasic", "success")
                    }
                    .addOnFailureListener {
                        Log.d("addLikedRecipeInBasic", "fail")
                    }
            }
            false -> {
                currentUserRef.collection("History").document("Like")
                    .update("communityRecipe", FieldValue.arrayRemove(documentName))
                    .addOnSuccessListener {
                        Log.d("removeLikedRecipeInBasic", "success")
                    }
                    .addOnFailureListener {
                        Log.d("removeLikedRecipeInBasic", "fail")
                    }
            }
        }
    }

    private fun deletePost(uidForPhoto: MutableList<String?>, postTimeStamp: String, havePhotoIndex: MutableList<Int?>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete post").setMessage("Do you want to delete post?")
        builder.setNegativeButton("Delete"){_, _ ->
            val deletePath = Firebase.auth.currentUser!!.uid.chunked(10)[0] + " " + postTimeStamp

            db.collection("Post").document(deletePath).delete()
                .addOnSuccessListener {
                    Log.d("delete Post Successfully", deletePath)

                    //User History에서 삭제
                    val email = FirebaseAuth.getInstance().currentUser?.email
                    db.collection("Users").document(email.toString())
                        .collection("History").document("Posting")
                        .update("posting", FieldValue.arrayRemove(deletePath))
                        .addOnSuccessListener {
                            Log.d("delete History posting", "success")
                        }
                        .addOnFailureListener {
                            Log.d("delete History posting", "fail")
                        }

                    //storage에 첨부된 사진이 있을시 삭제
                    if(havePhotoIndex.size != 0){
                        for(index in havePhotoIndex){
                            val deletePath = "${uidForPhoto[0]} $postTimeStamp $index"
                            val storageRef = Firebase.storage.reference
                            storageRef.child(deletePath).delete()
                                .addOnSuccessListener {
                                    Log.d("delete storage success", deletePath)
                                }
                                .addOnFailureListener {
                                    Log.d("delete storage fail", deletePath)
                                }
                        }
                    }

                    val deleteIntent = Intent(this, CommunityMainActivity::class.java)
                    deleteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(deleteIntent)
                }

                .addOnFailureListener { Log.d("delete Post Failed", deletePath) }
        }
        builder.setPositiveButton("Cancel", null)
        builder.create()
        builder.show()
}

class OrderRecyclerAdapter(private val orderList:MutableList<Any?>, private val havePhotoIndex:MutableList<Int?>
, private val uid:MutableList<String?>, private val timestamp:MutableList<String?>,val activity: AppCompatActivity) : RecyclerView.Adapter<OrderRecyclerAdapter.Holder>(){

    class Holder(val binding: CommunityOrderRecyclerBinding):RecyclerView.ViewHolder(binding.root){

        @RequiresApi(Build.VERSION_CODES.N)
        fun set(order:Any?, position: Int, havePhotoIndex: MutableList<Int?>, uid:String?, timestamp: String?, activity: AppCompatActivity) {
            Log.d("i'm in holder.set", order.toString())
            val storageRef = Firebase.storage.reference
            with(binding){
                textOrderText.text = order.toString()
                textOrderNum.text = (position+1).toString()
                Log.d("set text in detail order", order.toString())

                if(havePhotoIndex.size != 0 ){
                    for(photoIndex in havePhotoIndex){
                        if((position+1) == photoIndex){
                            Log.d("uid", uid!!)
                            Log.d("timestamp", timestamp!!)
                            val path = storageRef.child("$uid $timestamp $photoIndex")
                            Log.d("storage path in detail activity", "$uid $timestamp $photoIndex")
                            imageViewOrder.clipToOutline = true
                            path.downloadUrl.addOnCompleteListener {
                                if (it.isSuccessful){

                                    if( !activity.isDestroyed ){
                                        Glide.with(this.imageViewOrder)
                                            .load(it.result)
                                            .override(600,600)
                                            .fitCenter()
                                            .into(imageViewOrder)
                                    }

                                }
                            }
                            imageViewOrder.visibility = View.VISIBLE
                            imageViewOrder.scaleType = ImageView.ScaleType.FIT_CENTER
                        }
                    }
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = CommunityOrderRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: Holder, position: Int) {
        Log.d("i'm in onBindViewHolder", position.toString())
        try{
            val order = orderList[position]
            Log.d("get order from orderList", order.toString())
            holder.set(order, position, havePhotoIndex, uid[0], timestamp[0], activity)
        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }
    override fun getItemCount(): Int {
        return orderList.size
    }
}
}

class IngredientRecyclerAdapter(private val ingredientNameList: MutableList<String?>, private val ingredientAmountList: MutableList<String?>)
    : RecyclerView.Adapter<IngredientRecyclerAdapter.Holder>(){
    class Holder(val binding: CommunityIngredientRecyclerBinding):RecyclerView.ViewHolder(binding.root){
        fun set(ingredientName: String?, ingredientAmount: String?) {
            binding.textViewIngredientName.text = ingredientName.toString()
            binding.textViewIngredientAmount.text = ingredientAmount.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = CommunityIngredientRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        try{
            val ingredientName = ingredientNameList[position]
            val ingredientAmount = ingredientAmountList[position]
            holder.set(ingredientName, ingredientAmount)
        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return ingredientNameList.size
    }

}