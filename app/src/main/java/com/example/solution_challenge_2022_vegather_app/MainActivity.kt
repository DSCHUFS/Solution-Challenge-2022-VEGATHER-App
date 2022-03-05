package com.example.solution_challenge_2022_vegather_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Adapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityMainBinding
import com.example.solution_challenge_2022_vegather_app.databinding.MainPageMoreRecipeRecyclerBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    val binding by lazy{ ActivityMainBinding.inflate(layoutInflater)}

    private lateinit var db: FirebaseFirestore
    private val recipeInfo = ArrayList<RecipeInformation>()
    private var todayRecipeIndex : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        getRecipeDataFromFireBase()

        changeUiBarColor()

        binding.searchBar.setOnClickListener(){
            switchActivity("Search")
        }

        binding.seeMore.setOnClickListener {
            switchActivity("Recipe")
        }

       binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
           // 탭 버튼을 선택할 때
           override fun onTabSelected(tab: TabLayout.Tab?) {
               switchActivity(tab?.text.toString())
           }
            // 다른 탭 버튼을 눌러 선택된 탭 버튼이 해제될 때 이벤트
           override fun onTabUnselected(tab: TabLayout.Tab?) {
           }
            // 선택된 탭 버튼을 다시 선택할 때 이벤트
           override fun onTabReselected(tab: TabLayout.Tab?) {
                switchActivity(tab?.text.toString())
           }
       })
    }


    private fun changeUiBarColor(){
        val uiBarCustom = UiBar(window)
        uiBarCustom.setNaviBarIconColor(isBlack = true)
        uiBarCustom.setStatusBarIconColor(isBlack = true)
    }

    private fun getRecipeDataFromFireBase(){
        db.collection("Recipe")
            .get()
            .addOnSuccessListener {
                convertDocumentToRecipeInformation(it.documents)
                val todayRecipe = getRandomRecipeData(it.documents)
                setTodayRecipe(todayRecipe)
                setMoreRecipe(it.documents)
                setDataChangedListener()
            }
    }

    // DB의 데이터를 사용할 수 있게 data class 형태로 만들어서 뷰 객체에 데이터를 기입한다.
    private fun convertDocumentToRecipeInformation(recipeData : MutableList<DocumentSnapshot>){
        for (i in 0 until recipeData.size){
            recipeData[i].toObject(RecipeInformation::class.java)?.let { recipeInfo.add(it) }
        }
    }

    private fun getRandomRecipeData(recipeData: MutableList<DocumentSnapshot>): RecipeInformation {
        val position = (0 until recipeData.size-1).random()
        todayRecipeIndex = position
        return recipeInfo[todayRecipeIndex!!]
    }

    private fun setDataChangedListener(){
        db.collection("Recipe").document("${binding.todaysfoodName.text}")
            .addSnapshotListener { value, error ->
                val recipe = value?.toObject(RecipeInformation::class.java)
                binding.todaysLike.text = recipe?.like.toString()
            }
    }

    private fun setTodayRecipe(todayRecipe: RecipeInformation){
        binding.todaysfoodName.text = todayRecipe.name
        binding.todaysFoodIntroduce.text = todayRecipe.introduce
        binding.todaysKcal.text = todayRecipe.nutrition[1] + "Kcal"
        binding.todaysLike.text = todayRecipe.like.toString()
    }

    // 더 많은 레시피들은 오늘의 레시피 이외의 레시피들을 보여줘야 한다.
    private fun createMoreRecipe(adapter : MoreRecipeAdapter){
        for (i in 0 until recipeInfo.size){
            if( i != todayRecipeIndex ){
                adapter.appendRecipeData(recipeInfo[i])
            }
        }
    }

    private fun setMoreRecipe(recipeData : MutableList<DocumentSnapshot>){
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = MoreRecipeAdapter(MainPageMoreRecipeRecyclerBinding.inflate(layoutInflater))
        createMoreRecipe(adapter)
        adapter.loadParentActivity(this)
        binding.recyclerView.adapter = adapter
    }

    private fun switchActivity(activity : String){
        when(activity){
            "Community" ->{
                val intentCommunity = Intent(this, CommunityMainActivity::class.java)
                startActivity(intentCommunity)
            }
            "Mypage" ->{
                val intentMypage = Intent(this, MypageActivity::class.java)
                startActivity(intentMypage)
            }
            "Search" ->{
                val intentSearch = Intent(this,SearchActivity::class.java)
                intentSearch.putParcelableArrayListExtra("recipeData",recipeInfo)
                startActivity(intentSearch)
            }
            "Recipe" ->{
                val intentRecipe = Intent(this,RecipeMainActivity::class.java)
                intentRecipe.putExtra("recipeInfo",recipeInfo[todayRecipeIndex!!])
                startActivity(intentRecipe)
            }
        }
    }
}

