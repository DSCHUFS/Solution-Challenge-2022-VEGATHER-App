package com.example.solution_challenge_2022_vegather_app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.FragmentMyRecordBasicBinding
import com.example.solution_challenge_2022_vegather_app.databinding.MainPageMoreRecipeRecyclerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class MyRecordBasicFragment(category : String) : Fragment() {
    private var binding : FragmentMyRecordBasicBinding? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var user : FirebaseUser
    private val recipeInfo = ArrayList<RecipeInformation>()
    private lateinit var currentUserRef : DocumentReference
    private lateinit var myRecordActivity : MyRecordActivity
    private val ct = category

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance() //현재 로그인한 사용자 가져오기
        user = auth.currentUser!!
        currentUserRef = db.collection("Users").document(user.email.toString())
        myRecordActivity = context as MyRecordActivity
    }


    // DB의 데이터를 사용할 수 있게 data class 형태로 만들어서 뷰 객체에 데이터를 기입한다.
    private fun convertDocumentToRecipeInformation(recipeData : MutableList<DocumentSnapshot>){
        for (recipe in recipeData){
            val convertedData = recipe.toObject(RecipeInformation::class.java)
            convertedData?.imgUrl = getRecipeImageUrl(convertedData?.name.toString())
            if (convertedData != null) {
                recipeInfo.add(convertedData)
            }
        }
    }
    private fun getRecipeImageUrl( recipeName : String ) : String {
        return "Recipe/${recipeName}.jpg"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMyRecordBasicBinding.inflate(inflater,container,false)
        val adapter = MoreRecipeAdapter(MainPageMoreRecipeRecyclerBinding.inflate(layoutInflater))

        currentUserRef.collection("History")
            .document(ct) //내 좋아요 목록 가져오기
            .get()
            .addOnSuccessListener {
                var likedList = when(ct){
                    "Like" -> it.toObject(HistoryLikedRecipe::class.java)
                    "Comment" -> it.toObject(HistoryCommentRecipe::class.java)
                    else -> null //posting 관련 data class 넣기
                }
                Log.d("like List ====>", likedList.toString())

                db.collection("Recipe") //레시피 목록 가져오기
                    .get()
                    .addOnSuccessListener { recipes->
                        var recipeList = recipes.documents
                        var likedrecipe = recipes.documents

                        for(r in recipeList){
                            when (likedList) {
                                is HistoryLikedRecipe -> if(!likedList?.basicRecipe?.contains(r.id)) likedrecipe.remove(r)
                                is HistoryCommentRecipe -> if(!likedList?.basicComment?.contains(r.id)) likedrecipe.remove(r)
                                else null //posting 관련 코드
                            }
                        }
                        binding!!.recyclerView.layoutManager = LinearLayoutManager(this.context)
                        convertDocumentToRecipeInformation(likedrecipe)
                        adapter.setData(recipeInfo)
                        adapter.loadParentActivity(myRecordActivity)
                        binding!!.recyclerView.adapter = adapter
                    }
            }
        return binding!!.root
    }

}