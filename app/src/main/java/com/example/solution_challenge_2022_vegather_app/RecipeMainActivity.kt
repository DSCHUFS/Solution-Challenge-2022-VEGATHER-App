package com.example.solution_challenge_2022_vegather_app

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityRecipeMainBinding
import com.example.solution_challenge_2022_vegather_app.databinding.IngredientRecyclerBinding
import com.example.solution_challenge_2022_vegather_app.databinding.OrderRecyclerBinding

class RecipeMainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRecipeMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        window.apply {
//            decorView.systemUiVisibility =
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//            statusBarColor = Color.TRANSPARENT
//        }
        val customUiBar = UiBar(window)
        customUiBar.setStatusBarTransparent()
        customUiBar.setStatusBarIconColor(isBlack = true)
        customUiBar.setNaviBarIconColor(isBlack = true)


        // 현재 액티비티를 종료시키고 이전 액티비티로 이동한다.
        binding.imageButton2.setOnClickListener {
            finish()
        }

        // 메인 페이지에서 음식을 선택하면 선택한 음식의 정보를 레시피 액티비티에서 넘겨받는다.
        val intent = intent
        val callNumber = intent.getIntExtra("callNumber",0)
        val foodName = getFoodNameFromCall(intent)
        binding.textView27.text = foodName

        binding.textView26.setOnClickListener {
            val commentIntent = Intent(this,CommentActivity::class.java)
            startActivity(commentIntent)
        }



        // 재료와 레시피 제작 순서의 리사이클러 뷰를 연결한다.
        connectOrderAdapter(binding)
        connectIngredientsAdapterWithOrientation("horizontal",binding)

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            Log.d("id",checkedId.toString())
            when(checkedId) {
                R.id.radioButton2 -> connectIngredientsAdapterWithOrientation("horizontal",binding)
                R.id.radioButton3 -> connectIngredientsAdapterWithOrientation("vertical",binding)
            }
        }
    }

    private fun connectIngredientsAdapterWithOrientation(layout : String, binding : ActivityRecipeMainBinding){
        when(layout){
            "horizontal" -> binding.ingredientRecycler.layoutManager = LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false)

            "vertical" -> binding.ingredientRecycler.layoutManager = LinearLayoutManager(
                this)
        }
        val adapter = IngredientAdapter(IngredientRecyclerBinding.inflate(layoutInflater))
        adapter.getData()
        binding.ingredientRecycler.adapter = adapter
    }

    private fun connectOrderAdapter(binding : ActivityRecipeMainBinding){
        binding.orderRecycler.layoutManager = LinearLayoutManager(this)
        val adapter = OrderAdapter(OrderRecyclerBinding.inflate(layoutInflater))
        adapter.createTestData()
        binding.orderRecycler.adapter = adapter
    }

    private fun getFoodNameFromCall(intent : Intent) : String {
        return when(getCallNumber(intent)){
            1 -> intent.getStringExtra("foodName").toString()
            2 -> intent.getStringExtra("foodNameFromAdapter").toString()
            else -> "None"
        }
    }

    private fun getCallNumber(intent : Intent) : Int {
        val fromMainActivity = intent.getIntExtra("callNumber",0)
        val fromMoreRecipeAdapter = intent.getIntExtra("callNumberFromAdapter",0)

        return if( fromMainActivity!=0 ){
            fromMainActivity
        } else{
            fromMoreRecipeAdapter
        }
    }

    private fun setStatusBarIconColor(isBlack : Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // android os 12에서 사용 가능
            window.insetsController?.setSystemBarsAppearance(
                if (isBlack) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // minSdk 6.0부터 사용 가능
            window.decorView.systemUiVisibility = if (isBlack) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                // 기존 uiVisibility 유지
                window.decorView.systemUiVisibility
            }
        }
    }

    private fun setNaviBarIconColor(isBlack: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // android os 12에서 사용 가능
            window.insetsController?.setSystemBarsAppearance(
                if (isBlack) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 내비바 아이콘 색상이 8.0부터 가능하므로 커스텀은 동시에 진행해야 하므로 조건 동일 처리.
            window.decorView.systemUiVisibility =
                if (isBlack) {
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

                } else {
                    // 기존 uiVisibility 유지
                    // -> 0으로 설정할 경우, 상태바 아이콘 색상 설정 등이 지워지기 때문
                    window.decorView.systemUiVisibility
                }
        }
    }
}