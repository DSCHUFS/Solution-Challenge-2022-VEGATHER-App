package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import com.example.solution_challenge_2022_vegather_app.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    val fragmentManager = supportFragmentManager
    var transaction = fragmentManager.beginTransaction()

    val fragmentSearchHistory = SearchRankingAndHistoryFragment()
    val fragmentSearchKeyword = SearchKeywordFragment()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        transaction.add(R.id.fragmentContainer,fragmentSearchHistory)
        transaction.commit()
        binding.editTextTextPersonName5.doOnTextChanged { text, start, before, count ->
            if( text.toString()!=""){
                changeFragment("searchKeyword")
            }
            else{
                changeFragment("searchHistory")
            }
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
            "searchKeyword" -> transaction.replace(R.id.fragmentContainer,fragmentSearchKeyword).commit()
            "searchHistory" -> transaction.replace(R.id.fragmentContainer,fragmentSearchHistory).commit()
        }
    }
}