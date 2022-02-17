package com.example.solution_challenge_2022_vegather_app

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.marginEnd
import androidx.core.view.marginRight
import androidx.gridlayout.widget.GridLayout
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommunityWriteBinding
import com.google.firebase.firestore.FirebaseFirestore


class CommunityWriteActivity : AppCompatActivity() {

    val binding by lazy{ActivityCommunityWriteBinding.inflate(layoutInflater)}

    //재료 나열 레이아웃
    private val ingredientLayout : GridLayout
    get() = findViewById(R.id.ingredientLayout)

    //재료 총 합을 저장할 변수
    var countIngredients = 0

    //조리순서 나열 레이아웃
    private val orderLayout : GridLayout
    get() = findViewById(R.id.orderLayout)

    //조리순서 총 합을 저장할 변수
    var countOrder = 0

    //조리법에 들어가는 사진의 총 수
    var photoNumForOrder = 0

    private lateinit var db: FirebaseFirestore

    @RequiresApi(Build.VERSION_CODES.N)
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
        db = FirebaseFirestore.getInstance()
        binding.btnDone.setOnClickListener{
            uploadRecipe()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun uploadRecipe() {

        val date = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy.MM.dd")
        val formattedDate = sdf.format(date)
        val newpost: MutableMap<String, Any> = HashMap()
        newpost["title"] = binding.editTextTitle.text.toString()
        newpost["subtitle"] = binding.editTextSubtitle.text.toString()
        newpost["like"] = 0
        newpost["comment"] = 0
        newpost["timestamp"] = formattedDate

        db.collection("Post").document()
            .set(newpost)
            .addOnSuccessListener {
                Log.d(TAG, "Upload new recipe successfully")
                val intent = Intent(this, CommunityMainActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener {
                e -> Log.d(TAG, "Error writing new recipe", e)
            }

    }

    //재료 추가 버튼이 눌리면 실행하는 함수
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

    //조리법 추가 버튼이 눌리면 실행되는 함수
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
            width = 250.dp
            height = 48.dp
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END

        }
        val orderPhoto = ImageButton(this).apply {
            id = countOrder
            setImageResource(R.drawable.camera_icon)
            scaleType = ImageView.ScaleType.FIT_CENTER
            background = Color.TRANSPARENT.toDrawable()

            setOnClickListener {
                photoNumForOrder = orderNumber.id
                setPhotoOrder(photoNumForOrder)
            }
        }
        orderLayout.addView(orderNumber)
        orderLayout.addView(orderComment)
        orderLayout.addView(orderPhoto, 48.dp, 48.dp)
    }

    //사진 추가가 눌리면 실행되는 함수
    private fun setPhotoOrder(orderNumber : Int) {
        val intent: Intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            when(requestCode){
                1 -> {
                    var currentImageUri : Uri? = data?.data
                    try{
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, currentImageUri)
//                        val photo = ImageView(this).apply{
//                            setImageBitmap(bitmap)
//                            scaleType = ImageView.ScaleType.FIT_XY
//                        }
                        val relativeLayout = layoutInflater.inflate(R.layout.photo_order, null) as RelativeLayout

                        val photoNum = relativeLayout.findViewById<View>(R.id.photoNumber) as TextView
                        photoNum.text = photoNumForOrder.toString()


                        val photo = relativeLayout.findViewById<View>(R.id.photo) as ImageView
                        photo.setImageBitmap(bitmap)
                        photo.scaleType = ImageView.ScaleType.FIT_XY

                        val layoutParams = RelativeLayout.LayoutParams(130.dp, 130.dp)
                        layoutParams.setMargins(0, 0, 5.dp, 5)
                        relativeLayout.layoutParams = layoutParams
                        binding.photoOrder.addView(relativeLayout)

                        val removePhoto = relativeLayout.findViewById<View>(R.id.removePhoto) as TextView
                        removePhoto.setOnClickListener {
                            binding.photoOrder.removeView(relativeLayout)
                        }
                    }
                    catch(e:Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    //재료 제거를 누르면 실행되는 함수
    private fun removeIngredient(){
        if(countIngredients <= 0) {
            Toast.makeText(this, "Can't remove under 0", Toast.LENGTH_SHORT).show()
        }
        else{
            ingredientLayout.removeViews(countIngredients*3-3, 3)
            countIngredients--
        }
    }

    //조리법 제거를 누르면 실행되는 함수
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