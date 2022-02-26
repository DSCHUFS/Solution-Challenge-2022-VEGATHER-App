package com.example.solution_challenge_2022_vegather_app

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream


class CommunityWriteActivity : PermissionActivity() {

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

    var ingredientNameForDB = mutableListOf<Any?>()
    var ingredientAmountForDB = mutableListOf<Any?>()
    var recipeForDB = mutableListOf<Any?>()

    private val postList = mutableListOf<Post>()
    private val photoList = mutableListOf<Bitmap>()

    private val PERM_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    private val REQ_STORAGE = 100

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        requirePermission(PERM_STORAGE, REQ_STORAGE)
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

    override fun permissionGranted(requestCode: Int) {
        when(requestCode){
            REQ_STORAGE -> {
                Toast.makeText(this, "Strorage 권한 승인 완료", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun permissionDenied(requestCode: Int) {
        when(requestCode){
            REQ_STORAGE -> {
                Toast.makeText(this, "Strorage 권한 승인 거부시 사진을 첨부할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }




    @RequiresApi(Build.VERSION_CODES.N)
    private fun uploadRecipe() {

        currentFocus?.clearFocus()
        binding.parentOfWrite.requestFocus()


        val date = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy.MM.dd")
        val formattedDate = sdf.format(date)
//        val newpost: MutableMap<String, Any> = HashMap()
//        newpost["title"] = binding.editTextTitle.text.toString()
//        newpost["subtitle"] = binding.editTextSubtitle.text.toString()
//        newpost["like"] = 0
//        newpost["comment"] = 0
//        newpost["timestamp"] = formattedDate
//
//        newpost["ingredientName"] = ingredientNameForDB
//        newpost["ingredientAmount"] = ingredientAmountForDB
//        newpost["recipe"] = recipeForDB

        val newpost = Post(title = binding.editTextTitle.text.toString(), subtitle = binding.editTextSubtitle.text.toString(), like = 0, comment = 0,
        timestamp = formattedDate, ingredientName = ingredientNameForDB, ingredientAmount = ingredientAmountForDB, recipe = recipeForDB)
        postList.add(newpost)

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

        uploadPhoto()

        ingredientNameForDB.clear()
        ingredientAmountForDB.clear()
        recipeForDB.clear()
        countIngredients = 0
        countOrder = 0
    }

    //재료 추가 버튼이 눌리면 실행하는 함수
    private fun addIngredient() {
        currentFocus?.clearFocus()
        binding.parentOfWrite.requestFocus()

        val ingredientNumber = TextView(this).apply{
            id = countIngredients+1
            text = (countIngredients+1).toString()
            width = 30.dp
            height = 48.dp
            textSize = 6.dp.toFloat()
            setTextColor(ContextCompat.getColor(context!!, R.color.main_green))
        }
        val ingredientName = EditText(this).apply{
            id = countIngredients+1
            hint = "Food name"
            width = 150.dp
            height = 48.dp

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
        //포커스가 해제되면 그 위의 내용을 DB로 저장할 리스트에 저장
        ingredientName.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
                ingredientNameForDB.add(ingredientName.text.toString())
            }
        }
        ingredientAmount.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
                ingredientAmountForDB.add(ingredientAmount.text.toString())
            }
        }
        ingredientLayout.addView(ingredientNumber)
        ingredientLayout.addView(ingredientName)
        ingredientLayout.addView(ingredientAmount)
//        ingredientLayout.addView(ingredientRemove, 48.dp, 48.dp)

        countIngredients++
    }

    //조리법 추가 버튼이 눌리면 실행되는 함수
    private fun addOrder(){
        currentFocus?.clearFocus()
        binding.parentOfWrite.requestFocus()

        val orderNumber = TextView(this).apply {
            id = countOrder+1
            text = (countOrder+1).toString()
            width = 30.dp
            height = 48.dp
            textSize = 6.dp.toFloat()
            setTextColor(ContextCompat.getColor(context!!, R.color.main_green))
        }
        val orderComment = EditText(this).apply {
            id = countOrder+1
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

        }

        orderComment.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
                recipeForDB.add(orderComment.text.toString())
            }
        }

        orderPhoto.setOnClickListener {
            if (checkSelfPermission(PERM_STORAGE[0]) == PackageManager.PERMISSION_GRANTED){
                photoNumForOrder = orderNumber.id
                setPhotoOrder(photoNumForOrder)
            }else{
                Toast.makeText(this, "Strorage 권한 승인 거부시 사진을 첨부할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }

        }
        orderLayout.addView(orderNumber)
        orderLayout.addView(orderComment)
        orderLayout.addView(orderPhoto, 48.dp, 48.dp)
        countOrder++
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
                        photoList.add(bitmap)
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


//                        val key = db.collection("post").document().id
//                        Log.d("storage key", key)


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

    private fun uploadPhoto(){
        val storage = Firebase.storage
        val storageRef = storage.reference
        val nameRef = storageRef.child("example")

        for (photo in photoList){
//            val bitmap2 = (photo.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            var uploadTask = nameRef.putBytes(data)
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...
            }
        }

    }

    //재료 제거를 누르면 실행되는 함수
    private fun removeIngredient(){
        if(countIngredients <= 0) {
            Toast.makeText(this, "Can't remove under 0", Toast.LENGTH_SHORT).show()
        }
        else{
            currentFocus?.clearFocus()
            binding.parentOfWrite.requestFocus()
            Log.d("ingredient Order", "${ingredientNameForDB.lastIndex}")
            Log.d("ingredient Order", "${ingredientAmountForDB.lastIndex}")
            ingredientLayout.removeViews(countIngredients*3-3, 3)
            ingredientNameForDB.removeAt(ingredientNameForDB.lastIndex)
            ingredientAmountForDB.removeAt(ingredientAmountForDB.lastIndex)
            countIngredients--
        }
    }

    //조리법 제거를 누르면 실행되는 함수
    private fun removeOrder(){
        if(countOrder <= 0) {
            Toast.makeText(this, "Can't remove under 0", Toast.LENGTH_SHORT).show()
        }
        else{
            currentFocus?.clearFocus()
            binding.parentOfWrite.requestFocus()
            Log.d("remove Recipe", "${recipeForDB.lastIndex}")
            orderLayout.removeViews(countOrder*3-3, 3)
            recipeForDB.removeAt(recipeForDB.lastIndex)
            countOrder--
        }
    }

    val Int.dp: Int
        get() {
            val metrics = resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
        }
}