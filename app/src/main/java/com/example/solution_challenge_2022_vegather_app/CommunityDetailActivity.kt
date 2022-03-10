package com.example.solution_challenge_2022_vegather_app

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommunityDetailBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommunityIngredientRecyclerBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommunityOrderRecyclerBinding
import com.facebook.internal.Mutable
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.lang.Exception

class CommunityDetailActivity : AppCompatActivity() {

    private lateinit var db : FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCommunityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uiBarCustom = UiBar(window)
        uiBarCustom.setStatusBarIconColor(isBlack = true)
        uiBarCustom.setNaviBarIconColor(isBlack = true)

        binding.textLike.text = intent.getIntExtra("like", -1).toString()
        binding.textComment.text = intent.getIntExtra("comment", -1).toString()
        binding.textNickname.text = intent.getStringExtra("nickname")


        binding.textComment.setOnClickListener {
            val commentIntent = Intent(this,CommentActivity::class.java)
            startActivity(commentIntent)
        }

        db = FirebaseFirestore.getInstance()
        val postInfo = intent.getStringExtra("post info").toString().split(" ")
        val testInfo = intent.getStringExtra("post info").toString()
        Log.d("get post info", testInfo)
        binding.textTimsStamp.text = postInfo[0]
        val postTimeStamp = postInfo[0] + " " + postInfo[1]
        val postTitle = testInfo.substring(20)

        Log.d(TAG, "$postTimeStamp")
        Log.d(TAG, "$postTitle")

        binding.imageButtonDeletePost.setOnClickListener {
            deletePost(postTimeStamp)
        }

        val orderList = mutableListOf<Any?>()
        var havePhotoIndex = mutableListOf<Int?>()
        var uidForPhoto = mutableListOf<String?>()
        var timestampForPhoto = mutableListOf<String?>()
        val orderAdapter = OrderRecyclerAdapter(orderList, havePhotoIndex, uidForPhoto, timestampForPhoto)

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

        //좋아요 버튼 작업

    }

    private fun deletePost(postTimeStamp: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete post").setMessage("Do you want to delete post?")
        builder.setNegativeButton("Delete"){_, _ ->
            val deletePath = Firebase.auth.currentUser!!.uid.chunked(10)[0] + " " + postTimeStamp
            db.collection("Post").document(deletePath).delete()
                .addOnSuccessListener { Log.d("delete Post Successfully", deletePath)
                    val deleteIntent = Intent(this, CommunityMainActivity::class.java)
                    startActivity(deleteIntent)
                }

                .addOnFailureListener { Log.d("delete Post Failed", deletePath) }
        }
        builder.setPositiveButton("Cancel", null)
        builder.create()
        builder.show()
}

class OrderRecyclerAdapter(private val orderList:MutableList<Any?>, private val havePhotoIndex:MutableList<Int?>
, private val uid:MutableList<String?>, private val timestamp:MutableList<String?>) : RecyclerView.Adapter<OrderRecyclerAdapter.Holder>(){
    class Holder(val binding: CommunityOrderRecyclerBinding):RecyclerView.ViewHolder(binding.root){

        @RequiresApi(Build.VERSION_CODES.N)
        fun set(order:Any?, position: Int, havePhotoIndex: MutableList<Int?>, uid:String?, timestamp: String?) {
            Log.d("i'm in holder.set", order.toString())
            val storageRef = Firebase.storage.reference
            if(order != null){
                with(binding){
                    textOrderText.text = order.toString()
                    textOrderNum.text = (position+1).toString()
                    Log.d("set text in detail order", order.toString())
                    if(havePhotoIndex.size != 0 && (position+1) == havePhotoIndex[0]){
                        Log.d("uid", uid!!)
                        Log.d("timestamp", timestamp!!)
                        val path = storageRef.child(uid + " " + timestamp + " " + havePhotoIndex[0].toString())
                        Log.d("storage path in detail activity", uid + " " + timestamp + " " + havePhotoIndex[0].toString())
                        path.downloadUrl.addOnCompleteListener {
                            if (it.isSuccessful){
                                Glide.with(this.imageViewOrder).load(it.result).into(imageViewOrder)
                            }
                        }
                        imageViewOrder.visibility = View.VISIBLE
                        imageViewOrder.scaleType = ImageView.ScaleType.FIT_CENTER
                        havePhotoIndex.removeAt(0)
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
            holder.set(order, position, havePhotoIndex, uid[0], timestamp[0])
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