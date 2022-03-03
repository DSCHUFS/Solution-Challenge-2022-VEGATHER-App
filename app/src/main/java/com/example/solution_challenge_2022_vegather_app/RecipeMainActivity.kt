package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityRecipeMainBinding
import com.example.solution_challenge_2022_vegather_app.databinding.IngredientRecyclerBinding
import com.example.solution_challenge_2022_vegather_app.databinding.OrderRecyclerBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RecipeMainActivity : AppCompatActivity() {

    val binding by lazy{ ActivityRecipeMainBinding.inflate(layoutInflater)}
    private var recipeInfo : RecipeInformation? = RecipeInformation()
    private var currentStatusOfLike = false
    private lateinit var currentUserRef : DocumentReference

    private lateinit var db: FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var user : FirebaseUser

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance() //현재 로그인한 사용자 가져오기
        user = auth.currentUser!!
        currentUserRef = db.collection("Users").document(user.email.toString())
        recipeInfo = intent.getParcelableExtra<RecipeInformation>("recipeInfo")

        // 유저가 좋아요한 레시피면 좋아요 표시를 활성화
        currentUserRef.collection("History")
            .document("Like")
            .get()
            .addOnSuccessListener {
                currentStatusOfLike = isRecipeLiked(it)
                updateLikeButtonColor(isLiked = currentStatusOfLike)
            }

        // 좋아요와 댓글 수는 실시간으로 업데이트가 필요한 변수이므로 리스너를 추가
        db.collection("Recipe").document("${recipeInfo?.name}")
            .addSnapshotListener { value, error ->
                val latestRecipeInfo = value?.toObject(RecipeInformation::class.java)
                if (latestRecipeInfo != null) {
                    updateLikeCount(latestRecipeInfo.like)
                    updateCommentCount(latestRecipeInfo.comment)
                }
            }

        binding.imageButton2.setOnClickListener {
            finish()
        }

        changeUiBarColor()

        setFixedRecipeData(intent)

        binding.likeButton.setOnClickListener {
            currentStatusOfLike = !currentStatusOfLike
            updateLike(isLiked = currentStatusOfLike)
            updateLikeButtonColor(currentStatusOfLike)
        }

        binding.recipeComment.setOnClickListener {
            loadCommentActivity()
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.radioButton2 -> connectIngredientsAdapterWithOrientation("horizontal")
                R.id.radioButton3 -> connectIngredientsAdapterWithOrientation("vertical")
            }
        }
    }



    private fun changeUiBarColor(){
        val customUiBar = UiBar(window)
        customUiBar.setStatusBarTransparent()
        customUiBar.setStatusBarIconColor(isBlack = true)
        customUiBar.setNaviBarIconColor(isBlack = true)
    }

    private fun loadCommentActivity(){
        val commentIntent = Intent(this,CommentActivity::class.java)
        commentIntent.putExtra("commentCount",recipeInfo?.comment)
        commentIntent.putExtra("recipe",recipeInfo?.name)
        startActivity(commentIntent)
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

    private fun setFixedRecipeData(intent : Intent){
        binding.recipeName.text = recipeInfo?.name
        binding.recipeIntroduce.text = recipeInfo?.introduce
        binding.recipeLike.text = recipeInfo?.like.toString()
        binding.recipeComment.text = recipeInfo?.comment.toString()
        binding.recipeNutrition1.text = recipeInfo?.nutrition?.get(0)
        binding.recipeNutrition2.text = recipeInfo?.nutrition?.get(1)
        binding.recipeNutrition3.text = recipeInfo?.nutrition?.get(2)
        binding.recipeNutrition4.text = recipeInfo?.nutrition?.get(3)
        connectOrderAdapter()
        connectIngredientsAdapterWithOrientation("horizontal")
    }

    private fun isRecipeLiked(likedRecipe: DocumentSnapshot) : Boolean {
        val likedList = likedRecipe.toObject(HistoryLikedRecipe::class.java)
        Log.d("isP",likedRecipe.get("basicRecipe").toString())
        Log.d("isP",likedList?.basicRecipe?.contains(recipeInfo?.name).toString())
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

    private fun updateLikeCount(number : Int){
        recipeInfo?.like = number
        binding.recipeLike.text = number.toString()
    }

    private fun updateCommentCount(number : Int){
        recipeInfo?.comment = number
        binding.recipeComment.text = number.toString()
    }

    private fun updateLikeButtonColor(isLiked : Boolean){
        when(isLiked){
            true -> binding.likeButton
                .setColorFilter(Color.parseColor("#E16D64"))
            false -> binding.likeButton
                .setColorFilter(Color.parseColor("#BCBCBC"))
        }
    }
}