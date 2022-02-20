package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.example.solution_challenge_2022_vegather_app.databinding.ActivitySearchBinding

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


        // 완료 버튼을 누르면 포커싱 해제 -> 결과화면으로 이동을 위해 키보드를 내림
        binding.editTextTextPersonName5.setOnEditorActionListener{ textView, action, event ->
            var handled = false

            if (action == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(binding)
                handled = true
                clearFocusSearchbar(binding)
            }
            handled
        }

        // 프래그먼트 영역의 default xml은 인기검색어와 검색기록이어야 한다.
        transaction.add(R.id.fragmentContainer,fragmentSearchHistory).commitNow()

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

        binding.editTextTextPersonName5.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            if( actionId == EditorInfo.IME_ACTION_SEARCH ){
                val fragmentSearchResult = SearchResultFragment()
                transaction = fragmentManager.beginTransaction()
                transaction.remove(fragmentSearchHistory)
                transaction.remove(fragmentSearchKeyword)
                transaction.replace(R.id.fragmentContainer,fragmentSearchResult)
                           .addToBackStack(null)
                           .commit()
                v.clearFocus()
                hideKeyboard(binding)
                handled = true
            }
            handled
        }
    }

    private fun hideKeyboard(binding : ActivitySearchBinding){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.editTextTextPersonName5.windowToken, 0)
    }

    private fun clearFocusSearchbar(binding : ActivitySearchBinding){
        binding.editTextTextPersonName5.clearFocus()
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
            "searchHistory" -> transaction.replace(R.id.fragmentContainer,fragmentSearchHistory).commit()
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

}