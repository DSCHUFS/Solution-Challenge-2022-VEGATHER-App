package com.example.solution_challenge_2022_vegather_app

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommunityDetailBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommunityOrderRecyclerBinding
import com.example.solution_challenge_2022_vegather_app.databinding.CommunityRecyclerBinding
import com.google.firebase.firestore.FirebaseFirestore
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

        binding.textComment.setOnClickListener {
            val commentIntent = Intent(this,CommentActivity::class.java)
            startActivity(commentIntent)
        }
        db = FirebaseFirestore.getInstance()
        val postInfo = intent.getStringExtra("post info").toString().split(" ")
        binding.textTimsStamp.text = postInfo[0]
        val postTimeStamp = postInfo[0] + " " + postInfo[1]
        val postTitle = postInfo[2]
        Log.d(TAG, "$postTimeStamp $postTitle")

//        val havePhoto = intent.getStringArrayListExtra("have photo")
//        Log.d("have Photo", havePhoto.toString())


        val ingredientNameList = mutableListOf<Any?>()
        val ingredientAmountList = mutableListOf<Any?>()
        val orderList = mutableListOf<Any?>()
        var havePhotoIndex = mutableListOf<Int?>()
        val orderAdapter = OrderRecyclerAdapter(orderList)

        db.collection("Post").whereEqualTo("timestamp", postTimeStamp)
//            .whereEqualTo("title", postTitle)
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
                }
                orderAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.d(TAG,"error in get from db")
            }

        binding.orderRecycler.adapter = orderAdapter
        binding.orderRecycler.layoutManager = LinearLayoutManager(this)
    }

}

class OrderRecyclerAdapter(private val orderList:MutableList<Any?>) : RecyclerView.Adapter<OrderRecyclerAdapter.Holder>(){
    class Holder(val binding: CommunityOrderRecyclerBinding):RecyclerView.ViewHolder(binding.root){

        @RequiresApi(Build.VERSION_CODES.N)
        fun set(order:Any?, position: Int) {
            Log.d("i'm in holder.set", order.toString())
            if(order != null){
                with(binding){
                    textOrderText.text = order.toString()
                    textOrderNum.text = (position+1).toString()
                    Log.d("set text in detail order", order.toString())
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
            holder.set(order, position)
        }
        catch (e:Exception){
            e.printStackTrace()
        }

    }

    override fun getItemCount(): Int {
        return orderList.size
    }


}