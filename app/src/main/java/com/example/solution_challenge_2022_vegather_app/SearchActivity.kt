package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommentBinding
import com.example.solution_challenge_2022_vegather_app.databinding.ActivitySearchBinding
import com.google.android.material.snackbar.Snackbar

class SearchActivity : AppCompatActivity(), SelectedSearchHistoryListener{

    val binding by lazy { ActivitySearchBinding.inflate(layoutInflater) }

    private var bundle = Bundle()

    private var recipeInfo = ArrayList<RecipeInformation>()
    private val relatedSearchWord = ArrayList<RecipeInformation>()
    private val startIndex = ArrayList<Int>()
    private var inputSearchLength = 0
    private var inputValue : String? = null

    private val fragmentManager = supportFragmentManager
    private var transaction = fragmentManager.beginTransaction()

    private var fragmentSearchHistory = SearchRankingAndHistoryFragment(this)
    private var fragmentSearchKeyword = SearchKeywordFragment(this)
    private var fragmentSearchResult = SearchResultFragment()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        recipeInfo = intent.getParcelableArrayListExtra<RecipeInformation>("recipeData")
                as ArrayList<RecipeInformation>

        setUiBarColor(isBlack = true)
        binding.searchInputTextBar.requestFocus()
        // 프래그먼트 영역의 default xml은 인기검색어와 검색기록이어야 한다.
        transaction.add(R.id.fragmentContainer,fragmentSearchHistory).commitNow()

        binding.backMainButton.setOnClickListener(){
            if( fragmentManager.backStackEntryCount==0 ){
                finish()
            }
            else{
                fragmentManager.popBackStack()
                binding.searchInputTextBar.text = null
            }
        }

        // 사용자가 입력한 검색어에 연관된 키워드를 제공하기 위해서는 프래그먼트간의 전환은 필수적이다.
        binding.searchInputTextBar.doOnTextChanged { text, start, before, count ->
            if( text.toString().isNotEmpty() ){
                appendSimilarWord(text.toString())

                if( relatedSearchWord.isNotEmpty() ){
                    inputSearchLength = text.toString().length
                    changeFragment("searchKeyword")
                }
                else{
                    detachKeywordFragment()
                }
            }
            else{
                detachKeywordFragment()
            }
        }

        // 입력값이 없을 경우에는 검색어를 입력하라고 알려주어야 한다.
        binding.searchInputTextBar.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            if( actionId == EditorInfo.IME_ACTION_SEARCH ){
                inputValue = v.text.toString()
                if( v.text.isEmpty() ){
                    printSnackFromViewAndBinding(v,binding)
                }
                else{
                    v.clearFocus()
                    hideKeyboard(binding)
                    changeFragment("searchResult")
                }
                handled = true
            }
            handled
        }
    }

    // 1. 프래그먼트 전환 작업 ( 검색어 입력시에 발생되는 화면 전환 담당 )

    private fun changeFragment(name : String){
        transaction = fragmentManager.beginTransaction()
        when(name){
            // 검색어 자동완성을 위해서 키 입력마다 계속해서 프래그먼트를 초기화해야한다. 그 과정에서 검색어 관련 정보를 넘긴다.
            "searchKeyword" -> {
                transaction.remove(fragmentSearchKeyword).commitNow()
                sendDataToNextFragment(fragmentSearchKeyword)
                transaction.add(R.id.fragmentContainer,fragmentSearchKeyword).commitNow()
            }
            // 검색버튼을 누르면 검색결과 화면만 보여야 한다.
            "searchResult" -> {
                sendDataToNextFragment(fragmentSearchResult)
                transaction.remove(fragmentSearchHistory)
                transaction.remove(fragmentSearchKeyword)
                transaction.replace(R.id.fragmentContainer,fragmentSearchResult)
                    .addToBackStack("result")
                    .commit()
            }
        }
    }

    private fun createDataBundle(){
        bundle = Bundle()
        bundle.putParcelableArrayList("foodNameList",relatedSearchWord)
        bundle.putIntegerArrayList("startIndex",startIndex)
        bundle.putInt("inputSearchLength",inputSearchLength)
    }

    private fun sendDataToNextFragment(fragment : Fragment){
        createDataBundle()
        when(fragment){
            fragmentSearchKeyword -> {
                fragmentSearchKeyword = SearchKeywordFragment(this)
                fragmentSearchKeyword.arguments = bundle
            }
            fragmentSearchResult -> {
                fragmentSearchResult = SearchResultFragment()
                fragmentSearchResult.arguments = bundle
            }
        }
    }

    private fun detachKeywordFragment(){
        transaction = fragmentManager.beginTransaction()
        transaction.detach(fragmentSearchKeyword).commitNow()
    }

    override fun onSearchHistorySelected(keyword: String) {
        appendSimilarWord(keyword)
        changeFragment("searchResult")
        binding.searchInputTextBar.clearFocus()
        hideKeyboard(binding)
    }

    // 2. 데이터 분류

    private fun appendSimilarWord(food : String){
        relatedSearchWord.clear()
        startIndex.clear()

        for ( recipe in recipeInfo){
            val startPosition = recipe.name.indexOf(food)
            if( startPosition!=-1 ){
                relatedSearchWord.add(recipe)
                startIndex.add(startPosition)
            }
        }
    }

    // 3. 부가적인 작업 ( 서브 )

    private fun makeSnack(view : View){
        val snack = Snackbar.make(view,"Please enter the search word.", Snackbar.LENGTH_SHORT)
        snack.setTextColor(Color.WHITE)
        snack.view.setBackgroundResource(R.drawable.mypage_top_background)
        snack.show()
    }

    private fun printSnackFromViewAndBinding(v : TextView, binding : ActivitySearchBinding){
        v.clearFocus()
        showKeyboard(binding)
        makeSnack(binding.searchContainer)
    }

    private fun hideKeyboard(binding : ActivitySearchBinding){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.searchInputTextBar.windowToken, 0)
    }

    private fun showKeyboard(binding : ActivitySearchBinding){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.searchInputTextBar,0)
    }

    // 사용자가 검색창 이외의 화면을 터치하면 키보드를 내려서 화면을 가리지 않도록 한다.
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val focusView = currentFocus
        if( focusView!=null ){
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev!!.x.toInt()
            val y = ev.y.toInt()
            if (!rect.contains(x, y)) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun setUiBarColor(isBlack : Boolean){
        val uiBarCustom = UiBar(window)
        uiBarCustom.setStatusBarIconColor(isBlack)
        uiBarCustom.setNaviBarIconColor(isBlack)
    }
}