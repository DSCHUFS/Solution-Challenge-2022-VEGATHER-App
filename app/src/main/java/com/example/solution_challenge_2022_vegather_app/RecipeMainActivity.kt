package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.view.marginTop
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityRecipeMainBinding
import com.example.solution_challenge_2022_vegather_app.databinding.IngredientRecyclerBinding
import com.example.solution_challenge_2022_vegather_app.databinding.OrderRecyclerBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RecipeMainActivity : AppCompatActivity() {

    val binding by lazy{ ActivityRecipeMainBinding.inflate(layoutInflater)}
    private var recipeInfo : RecipeInformation? = RecipeInformation()
    private var currentStatusOfLike = false

    private lateinit var currentUserRef : DocumentReference
    private lateinit var storageRef : StorageReference
    private lateinit var db: FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var user : FirebaseUser

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        auth = FirebaseAuth.getInstance() //현재 로그인한 사용자 가져오기
        user = auth.currentUser!!
        currentUserRef = db.collection("Users").document(user.email.toString())
        recipeInfo = intent.getParcelableExtra<RecipeInformation>("recipeInfo")

        changeUiBarColor()

        // 유저가 좋아요한 레시피면 좋아요 표시를 활성화
        currentUserRef.collection("History")
            .document("Like")
            .get()
            .addOnSuccessListener {
                currentStatusOfLike = isRecipeLiked(it)
                updateLikeButtonColor(currentStatusOfLike)
            }

        // 좋아요와 댓글 수는 실시간으로 업데이트가 필요한 변수이므로 리스너를 추가
        db.collection("Recipe").document("${recipeInfo?.name}")
            .addSnapshotListener { value, error ->
                val latestRecipeInfo = value?.toObject(RecipeInformation::class.java)
                recipeInfo?.like = latestRecipeInfo?.like!!
                recipeInfo?.comment = latestRecipeInfo.comment

                binding.recipeLike.text = latestRecipeInfo.like.toString()
                binding.recipeComment.text = latestRecipeInfo.comment.toString()
            }

        getImageLoadingEffect()
        getRecipeData()
        connectOrderAdapter()
        connectIngredientsAdapterWithOrientation("horizontal")

        binding.likeButton.setOnClickListener {
            currentStatusOfLike = !currentStatusOfLike
            updateLike(isLiked = currentStatusOfLike)
            updateLikeButtonColor(currentStatusOfLike)
            if(currentStatusOfLike){
                MyApplication.prefs.setPrefs("Like", "Done")
                MyApplication.prefs.setIntPrefs("likeNum", MyApplication.prefs.getIntPrefs("likeNum", 0)+1)
            }
        }

        binding.commentContainer.setOnClickListener {
            loadCommentActivity()
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.radioButton2 -> connectIngredientsAdapterWithOrientation("horizontal")
                R.id.radioButton3 -> connectIngredientsAdapterWithOrientation("vertical")
            }
        }

        binding.imageButton2.setOnClickListener {
            finish()
        }

        val customUiBar = UiBar(window)
        binding.nestedScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if( scrollY < binding.reipceImage.height ){
                customUiBar.setStatusBarIconColor(isBlack = false)
            }
            else{
                customUiBar.setStatusBarIconColor(isBlack = true)
            }
        }
    }




    private fun connectIngredientsAdapterWithOrientation(layout : String){
        when(layout){
            "horizontal" -> binding.ingredientRecycler.layoutManager = LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false)

            "vertical" -> binding.ingredientRecycler.layoutManager = LinearLayoutManager(
                this)
        }

        val adapter = IngredientAdapter(IngredientRecyclerBinding.inflate(layoutInflater))
        recipeInfo?.let { adapter.setData(it) }
        binding.ingredientRecycler.adapter = adapter
    }

    private fun connectOrderAdapter(){
        binding.orderRecycler.layoutManager = LinearLayoutManager(this)
        val adapter = OrderAdapter(OrderRecyclerBinding.inflate(layoutInflater))
        recipeInfo?.let { adapter.setData(it) }
        binding.orderRecycler.adapter = adapter
    }

    private fun getImageLoadingEffect(){
        Glide.with(this)
            .load(R.drawable.loading_bigsize)
            .centerInside()
            .into(binding.reipceImage)
    }

    private fun getImage( url : String ){
        val imgRef = storageRef.child(url)
        imgRef.downloadUrl.addOnSuccessListener {
            Glide.with(this)
                .load(it)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.reipceImage)
        }
    }

    private fun getRecipeData(){
        binding.recipeName.text = recipeInfo?.name
        binding.recipeIntroduce.text = recipeInfo?.introduce
        binding.recipeLike.text = recipeInfo?.like.toString()
        binding.recipeComment.text = recipeInfo?.comment.toString()
        getImage(recipeInfo?.imgUrl.toString())
        binding.recipeNutrition1.text = recipeInfo?.nutrition?.get(0)
        binding.recipeNutrition2.text = recipeInfo?.nutrition?.get(1)
        binding.recipeNutrition3.text = recipeInfo?.nutrition?.get(2)
        binding.recipeNutrition4.text = recipeInfo?.nutrition?.get(3)
    }

    private fun isRecipeLiked(likedRecipe: DocumentSnapshot) : Boolean {
        val likedList = likedRecipe.toObject(HistoryLikedRecipe::class.java)
        return likedList?.basicRecipe?.contains(recipeInfo?.name) ?: false
    }

    private fun updateLikedRecipeInDataBase(isLiked : Boolean){
        when(isLiked){
            true -> {
                currentUserRef.collection("History").document("Like")
                    .update("basicRecipe", FieldValue.arrayUnion(recipeInfo?.name))
                    .addOnSuccessListener {
                        Log.d("addLikedRecipeInBasic", "success")
                    }
                    .addOnFailureListener {
                        Log.d("addLikedRecipeInBasic", "fail")
                    }
            }
            false -> {
                currentUserRef.collection("History").document("Like")
                    .update("basicRecipe", FieldValue.arrayRemove(recipeInfo?.name))
                    .addOnSuccessListener {
                        Log.d("removeLikedRecipeInBasic", "success")
                    }
                    .addOnFailureListener {
                        Log.d("removeLikedRecipeInBasic", "fail")
                    }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateLike(isLiked : Boolean) {
        val addedNum = if (isLiked) 1 else -1

        db.collection("Recipe")
            .document("${recipeInfo?.name}")
            .update("like", recipeInfo?.like?.plus(addedNum))
            .addOnSuccessListener {
                updateLikedRecipeInDataBase(isLiked)
            }
    }

    private fun updateLikeButtonColor(isLiked : Boolean){
        when(isLiked){
            true -> binding.likeButton
                .setColorFilter(Color.parseColor("#81E678"))
            false -> binding.likeButton
                .setColorFilter(Color.parseColor("#BCBCBC"))
        }
    }

    // 부가적인 작업 ( 서브 )

    private fun changeUiBarColor(){
        val customUiBar = UiBar(window)
        if( Build.VERSION.SDK_INT >= 30){
            customUiBar.setStatusBarTransparent()
        }
        else if( Build.VERSION.SDK_INT >= 23){
            customUiBar.setStatusBarIconColor(isBlack = false)
        }

    }

    private fun loadCommentActivity(){
        val commentIntent = Intent(this,CommentActivity::class.java)
        commentIntent.putExtra("recipe",recipeInfo?.name)
        startActivity(commentIntent)
    }
}