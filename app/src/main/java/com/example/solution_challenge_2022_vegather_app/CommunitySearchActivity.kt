package com.example.solution_challenge_2022_vegather_app

import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommunitySearchBinding

class CommunitySearchActivity : AppCompatActivity() {

    val binding by lazy{ActivityCommunitySearchBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val uiBarCustom = UiBar(window)
        uiBarCustom.setStatusBarIconColor(isBlack = true)
        uiBarCustom.setNaviBarIconColor(isBlack = true)

        //키보드 자동으로 올리기
        binding.searchBar.requestFocus()
        val imm : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
	    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        //Search 버튼이 눌리면 결과 액티비티로 이동
        binding.searchBar.setOnKeyListener { v, keyCode, event ->
            if((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KEYCODE_ENTER)){
                Log.d("Enter key pressed", binding.searchBar.text.toString())
                val resultIntent = Intent(this, CommunitySearchResultActivity::class.java)
                resultIntent.putExtra("search", binding.searchBar.text.toString())
                startActivity(resultIntent)
                true
            }else{
                false
            }
        }



    }
}