package com.example.solution_challenge_2022_vegather_app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.gridlayout.widget.GridLayout
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommunityWriteBinding


class CommunityWriteActivity : AppCompatActivity() {

    val binding by lazy{ActivityCommunityWriteBinding.inflate(layoutInflater)}

    private val ingredientLayout : GridLayout
    get() = findViewById(R.id.ingredientLayout)

    var countIngredients = 0

    private val orderLayout : GridLayout
    get() = findViewById(R.id.orderLayout)

    var countOrder = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnAddIngredient.setOnClickListener {
            addIngredient()
        }
        binding.btnAddOrder.setOnClickListener {
            addOrder()
        }
        binding.btnRemoveIngredient.setOnClickListener {
            removeIngredient()
        }
        binding.btnRemoveOrder.setOnClickListener {
            removeOrder()
        }
    }

    private fun addIngredient() {
        countIngredients++

        val ingredientNumber = TextView(this).apply{
            id = countIngredients
            text = countIngredients.toString()
            width = 30.dp
            height = 48.dp
            textSize = 6.dp.toFloat()
            setTextColor(ContextCompat.getColor(context!!, R.color.main_green))
            //Log.d("text id created", id.toString())
        }
        val ingredientName = EditText(this).apply{
            id = countIngredients
            hint = "Food name"
            width = 150.dp
            height = 48.dp
            //Log.d("Name id created", id.toString())
        }
        val ingredientAmount = EditText(this).apply{
            hint = "Amount"
            width = 150.dp
            height = 48.dp
        }
//        val ingredientRemove = ImageButton(this).apply{
//            setImageResource(R.drawable.minus)
//            background = Color.TRANSPARENT.toDrawable()
//            setOnClickListener {
//
//            }
//        }
        ingredientLayout.addView(ingredientNumber)
        ingredientLayout.addView(ingredientName)
        ingredientLayout.addView(ingredientAmount)
//        ingredientLayout.addView(ingredientRemove, 48.dp, 48.dp)
    }

    private fun addOrder(){
        countOrder++

        val orderNumber = TextView(this).apply {
            id = countOrder
            text = countOrder.toString()
            width = 30.dp
            height = 48.dp
            textSize = 6.dp.toFloat()
            setTextColor(ContextCompat.getColor(context!!, R.color.main_green))
        }
        val orderComment = EditText(this).apply {
            id = countOrder
            hint = "Add a comment"
            width = 300.dp
            height = 48.dp
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END

        }
        val orderPicture = ImageButton(this).apply {
            setImageResource(R.drawable.camera_icon)
            scaleType = ImageView.ScaleType.FIT_CENTER
            background = Color.TRANSPARENT.toDrawable()

            setOnClickListener {

            }
        }
        orderLayout.addView(orderNumber)
        orderLayout.addView(orderComment)
        orderLayout.addView(orderPicture, 48.dp, 48.dp)
    }



    private fun removeIngredient(){
        if(countIngredients <= 0) {
            Toast.makeText(this, "Can't remove under 0", Toast.LENGTH_SHORT).show()
        }
        else{
            ingredientLayout.removeViews(countIngredients*3-3, 3)
            countIngredients--
        }
    }
    private fun removeOrder(){
        if(countOrder <= 0) {
            Toast.makeText(this, "Can't remove under 0", Toast.LENGTH_SHORT).show()
        }
        else{
            orderLayout.removeViews(countOrder*3-3, 3)
            countOrder--
        }
    }

    val Int.dp: Int
        get() {
            val metrics = resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
        }
}