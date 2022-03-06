package com.example.solution_challenge_2022_vegather_app

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

class MyRecordBasicFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var user : FirebaseUser
    private val recipeInfo = ArrayList<RecipeInformation>()
    private var currentStatusOfLike = false
    private lateinit var currentUserRef : DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance() //현재 로그인한 사용자 가져오기
        user = auth.currentUser!!
        currentUserRef = db.collection("Users").document(user.email.toString())
    }

    private fun getRecipeDataFromFireBase(adapter : MoreRecipeAdapter){
        currentUserRef.collection("History")
            .document("Like") //내 좋아요 목록 가져오기
            .get()
            .addOnSuccessListener {
                var likedList = it.toObject(HistoryLikedRecipe::class.java)
                Log.d("test1", likedList.toString())

                db.collection("Recipe") //레시피 목록 가져오기
                    .get()
                    .addOnSuccessListener { recipes->
                        var recipeList = recipes.documents
                        var likedrecipe = recipes.documents

                        for(r in recipeList){
                            Log.d("test2", r.id)
                            if(likedList?.basicRecipe?.contains(r.id) == false){ //내가 좋아요 누른 레시피만 add
                                likedrecipe.remove(r)
                                Log.d("test3.1", likedrecipe.toString())
                            }
                        }
                        Log.d("test3", likedrecipe.toString())
                        convertDocumentToRecipeInformation(likedrecipe, adapter)
                    }
            }
    }

    // DB의 데이터를 사용할 수 있게 data class 형태로 만들어서 뷰 객체에 데이터를 기입한다.
    private fun convertDocumentToRecipeInformation(recipeData : MutableList<DocumentSnapshot>, adapter : MoreRecipeAdapter){
        for (i in 0 until recipeData.size){
            recipeData[i].toObject(RecipeInformation::class.java)?.let { recipeInfo.add(it) }
        }
        createMoreRecipe(adapter)
    }

    // 더 많은 레시피들은 오늘의 레시피 이외의 레시피들을 보여줘야 한다.
    private fun createMoreRecipe(adapter : MoreRecipeAdapter){
        for (i in 0 until recipeInfo.size){
            adapter.appendRecipeData(recipeInfo[i])
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentMyRecordBasicBinding.inflate(inflater,container,false)
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)

        val adapter = MoreRecipeAdapter(MainPageMoreRecipeRecyclerBinding.inflate(layoutInflater))
        getRecipeDataFromFireBase(adapter)
        this.context?.let { adapter.loadParentActivity(it) }
        binding.recyclerView.adapter = adapter
        Log.d("test4", "before createMoreRecipe")
        //createMoreRecipe(adapter)
        return binding.root
    }

}