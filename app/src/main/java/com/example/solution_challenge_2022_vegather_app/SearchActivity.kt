package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommentBinding
import com.example.solution_challenge_2022_vegather_app.databinding.ActivitySearchBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class SearchActivity : AppCompatActivity(), SelectedSearchHistoryListener{

    val binding by lazy { ActivitySearchBinding.inflate(layoutInflater) }

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser!!
    private val userRef: DocumentReference = db.collection("Users").document(user.email.toString())

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
        // ??????????????? ????????? default xml??? ?????????????????? ????????????????????? ??????.
        transaction.add(R.id.fragmentContainer,fragmentSearchHistory).commit()

        binding.backMainButton.setOnClickListener(){
            if( fragmentManager.backStackEntryCount==0 ){
                finish()
            }
            else{
                fragmentManager.popBackStack()
                binding.searchInputTextBar.text = null
            }
        }

        // ???????????? ????????? ???????????? ????????? ???????????? ???????????? ???????????? ????????????????????? ????????? ???????????????.
        binding.searchInputTextBar.doOnTextChanged { text, start, before, count ->
            if( text.toString().isNotEmpty() ){
                appendSimilarWord(text.toString())

                if( relatedSearchWord.isNotEmpty() ){
                    inputSearchLength = text.toString().length
                    changeFragment("searchKeyword")
                }
                else{
                    Log.d("?????????","?????????????????????")
                    detachKeywordFragment()
                }
            }
            else{
                detachKeywordFragment()
            }
        }

        // ???????????? ?????? ???????????? ???????????? ??????????????? ??????????????? ??????.
        binding.searchInputTextBar.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            if( actionId == EditorInfo.IME_ACTION_SEARCH ){
                inputValue = v.text.toString()
                if( v.text.isEmpty() || inputValue!!.trim().isEmpty() ){
                    binding.searchInputTextBar.text = null
                    printSnackFromViewAndBinding(v)
                }
                else{
                    v.clearFocus()
                    hideKeyboard()
                    changeFragment("searchResult")
                    addKeywordToSearchHistory(inputValue!!)
                }
                handled = true
            }
            handled
        }
    }

    // 1. ??????????????? ?????? ?????? ( ????????? ???????????? ???????????? ?????? ?????? ?????? )

    private fun changeFragment(name : String){
        transaction = fragmentManager.beginTransaction()
        when(name){
            // ????????? ??????????????? ????????? ??? ???????????? ???????????? ?????????????????? ?????????????????????. ??? ???????????? ????????? ?????? ????????? ?????????.
            "searchKeyword" -> {
                transaction.remove(fragmentSearchKeyword).commit()
                transaction = fragmentManager.beginTransaction()
                sendDataToNextFragment(fragmentSearchKeyword)
                transaction.add(R.id.fragmentContainer,fragmentSearchKeyword).commit()
            }
            // ??????????????? ????????? ???????????? ????????? ????????? ??????.
            "searchResult" -> {
                sendDataToNextFragment(fragmentSearchResult)
                transaction.replace(R.id.fragmentContainer,fragmentSearchResult).addToBackStack(null)
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
        transaction.detach(fragmentSearchKeyword).commit()
    }

    override fun onSearchHistorySelected(keyword: String) {
        appendSimilarWord(keyword)
        changeFragment("searchResult")
        binding.searchInputTextBar.clearFocus()
        hideKeyboard()
    }

    // 2. ????????? ??????

    // compRecipe??? recipe??? ????????? ????????? ????????? ??????, ????????? ?????? ???????????? ??????
    private fun getStartIndexSame( recipe : String, inputText: String) : Int {
        if( recipe.length < inputText.length){
            return -1
        }

        var position = -1
        var cnt = 0
        var i = 0
        var j = 0
        while (cnt<inputText.length && i < recipe.length){
            if( recipe[i].lowercase() == inputText[j].lowercase() ){
                if( cnt==0 ) { position=i }
                cnt++
                j++
            }
            else if( 0 < cnt && cnt < inputText.length ){
                return -1
            }
            i++
        }
        return if ( cnt==inputText.length ){
            Log.d("???????????? ???????????? ?????? ?????????",recipe)
            position
        }
        else -1
    }

    private fun appendSimilarWord(food : String){
        relatedSearchWord.clear()
        startIndex.clear()

        for ( recipe in recipeInfo){
//            val startPosition = recipe.name.indexOf(food)
            val startPosition = getStartIndexSame(recipe.name,food)
            getStartIndexSame(recipe.name,food)
            if( startPosition!=-1 ){
                relatedSearchWord.add(recipe)
                startIndex.add(startPosition)
            }
        }
    }

    // 3. ?????????????????? ?????? ( ???????????? ?????? )

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTime(): String {
        val now = System.currentTimeMillis()
        return DateFormat.format("yyyy.MM.dd kk:mm:ss",now).toString()
    }

    data class SearchHistory(
        val basicSearch : HashMap<String,String> = HashMap(),
        val communitySearch : HashMap<String,String> = HashMap()
    )

    private fun updateSearchHistory( updatedData : HashMap<String,String>){
        userRef.collection("History").document("Search")
            .update("basicSearch",updatedData)
    }

    private fun addKeywordToSearchHistory(text : String){
        userRef.collection("History").document("Search").get()
            .addOnSuccessListener {
                val searchHistoryHash = it.toObject(SearchHistory::class.java)?.basicSearch
                searchHistoryHash?.set(text, getCurrentTime())
                if (searchHistoryHash != null) {
                    updateSearchHistory(searchHistoryHash)
                }
            }
    }


    // 4. ???????????? ?????? ( ?????? )

    private fun makeSnack(view : View){
        val snack = Snackbar.make(view,"Please enter the search word.", Snackbar.LENGTH_SHORT)
        snack.setTextColor(Color.WHITE)
        snack.view.setBackgroundResource(R.drawable.mypage_top_background)
        snack.show()
    }

    private fun printSnackFromViewAndBinding(v : TextView){
        v.clearFocus()
        showKeyboard()
        makeSnack(binding.searchContainer)
    }

    private fun hideKeyboard(){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.searchInputTextBar.windowToken, 0)
    }

    private fun showKeyboard(){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.searchInputTextBar,0)
    }

    // ???????????? ????????? ????????? ????????? ???????????? ???????????? ????????? ????????? ????????? ????????? ??????.
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
    }
}