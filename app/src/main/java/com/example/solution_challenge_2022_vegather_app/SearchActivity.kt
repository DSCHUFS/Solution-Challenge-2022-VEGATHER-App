package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import com.example.solution_challenge_2022_vegather_app.databinding.ActivitySearchBinding
import com.google.android.material.snackbar.Snackbar

class SearchActivity : AppCompatActivity() {

    private var bundle = Bundle()

    private val foodData : ArrayList<FoodInfo> = createTestData()
    private val relatedSearchWord = ArrayList<String>()
    private val startIndex = ArrayList<Int>()

    private val fragmentManager = supportFragmentManager
    private var transaction = fragmentManager.beginTransaction()

    private val fragmentSearchHistory = SearchRankingAndHistoryFragment()
    private var fragmentSearchKeyword = SearchKeywordFragment()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uiBarCustom = UiBar(window)
        uiBarCustom.setStatusBarIconColor(isBlack = true)
        uiBarCustom.setNaviBarIconColor(isBlack = true)

        binding.imageButton.setOnClickListener(){
            finish()
        }

        binding.editTextTextPersonName5.requestFocus()
        binding.editTextTextPersonName5.setOnFocusChangeListener { v, hasFocus ->
            when(hasFocus){
                true -> binding.editTextTextPersonName5.hint = ""
                false -> binding.editTextTextPersonName5.hint = "Search"
            }
        }

        // 프래그먼트 영역의 default xml은 인기검색어와 검색기록이어야 한다.
        transaction.add(R.id.fragmentContainer,fragmentSearchHistory).commitNow()

        // 사용자가 입력한 검색어에 연관된 키워드를 제공하기 위해서는 프래그먼트간의 전환은 필수적이다.
        binding.editTextTextPersonName5.doOnTextChanged { text, start, before, count ->
            if( text.toString().isNotEmpty() ){
                relatedSearchWord.clear()
                startIndex.clear()
                appendSimilarWord(text.toString())

                if( relatedSearchWord.isNotEmpty() ){
                    bundle.putInt("inputSearchLength",text.toString().length)
                    changeFragment("searchKeyword")
                }
                else{
                    transaction = fragmentManager.beginTransaction()
                    transaction.detach(fragmentSearchKeyword).commitNow()
                }
            }
            else{
                transaction = fragmentManager.beginTransaction()
                transaction.detach(fragmentSearchKeyword).commitNow()
            }
        }

        // 입력값이 없을 경우에는 검색어를 입력하라고 알려주어야 한다.
        binding.editTextTextPersonName5.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            if( actionId == EditorInfo.IME_ACTION_SEARCH ){
                if (v.text.isEmpty() ){
                    v.clearFocus()
                    showKeyboard(binding)
                    printSnack(binding.searchContainer)
                }
                else{
                    changeFragment("searchResult")
                    v.clearFocus()
                    hideKeyboard(binding)
                    handled = true
                }
            }
            handled
        }
    }

    private fun hideKeyboard(binding : ActivitySearchBinding){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.editTextTextPersonName5.windowToken, 0)
    }

    private fun showKeyboard(binding : ActivitySearchBinding){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.editTextTextPersonName5,0)
    }

    // 사용자가 검색창 이외의 화면을 터치하면 키보드를 내려서 화면을 가리지 않도록 한다.
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        currentFocus?.clearFocus()
        return true
    }

    private fun changeFragment(name : String){
        transaction = fragmentManager.beginTransaction()
        when(name){
            // 검색어 자동완성을 위해서 키 입력마다 계속해서 프래그먼트를 초기화해야한다. 그 과정에서 검색어 관련 정보를 넘긴다.
            "searchKeyword" -> {
                transaction.remove(fragmentSearchKeyword).commitNow()
                fragmentSearchKeyword = SearchKeywordFragment()
                bundle.putStringArrayList("foodNameList",relatedSearchWord)
                bundle.putIntegerArrayList("startIndex",startIndex)
                fragmentSearchKeyword.arguments = bundle
                transaction.add(R.id.fragmentContainer,fragmentSearchKeyword).commitNow()
            }
            // 검색버튼을 누르면 검색결과 화면만 보여야 한다.
            "searchResult" -> {
                val fragmentSearchResult = SearchResultFragment()
                transaction.remove(fragmentSearchHistory)
                transaction.remove(fragmentSearchKeyword)
                transaction.replace(R.id.fragmentContainer,fragmentSearchResult)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun createTestData() : ArrayList<FoodInfo> {
        val foodData = ArrayList<FoodInfo>()
        foodData.add(FoodInfo("Tomato Pasta","test textSearching1",123))
        foodData.add(FoodInfo("Pascal","test textSearching2",42))
        foodData.add(FoodInfo("Matsta","test textSearching3",38))
        foodData.add(FoodInfo("Traspe Lemon","test textSearching4",22))
        foodData.add(FoodInfo("Salmon Salad","test textSearching5",999))
        foodData.add(FoodInfo("Master mapasta","test textSearching6",34))
        foodData.add(FoodInfo("Parameter Station","test textSearching7",733))
        foodData.add(FoodInfo("Photo Booth","test textSearching8",121))
        foodData.add(FoodInfo("Playlist Bread","test textSearching9",24))
        foodData.add(FoodInfo("Asparagas","test textSearching11",395))
        foodData.add(FoodInfo("Airbarn","test textSearching12",395))
        foodData.add(FoodInfo("Start","test textSearching10",395))
        foodData.add(FoodInfo("Vegan","test textSearching10",395))
        foodData.add(FoodInfo("Unbalance","test textSearching10",395))
        foodData.add(FoodInfo("Stranger","test textSearching10",395))

        return foodData
    }

    private fun appendSimilarWord(food : String){
        for ( foodInDataBase in foodData){
            val startPosition = foodInDataBase.foodNameData.indexOf(food)
            if( startPosition!=-1 ){
                relatedSearchWord.add(foodInDataBase.foodNameData)
                startIndex.add(startPosition)
            }
        }
    }

    private fun printSnack(view : View){
        val snack = Snackbar.make(view,"Please enter the search word.", Snackbar.LENGTH_SHORT)
        snack.setTextColor(Color.WHITE)
        snack.view.setBackgroundResource(R.drawable.mypage_top_background)
        snack.show()
    }

}