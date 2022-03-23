package com.example.solution_challenge_2022_vegather_app

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.icu.text.SimpleDateFormat
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.gridlayout.widget.GridLayout
import com.bumptech.glide.Glide
import com.example.solution_challenge_2022_vegather_app.databinding.ActivityCommunityWriteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.lang.Thread.sleep
import java.io.IOException


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

    private val photoList = mutableListOf<Bitmap?>()
    private val havePhotoList = mutableListOf("false")
    private lateinit var formattedDate : String

    private val PERM_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    private val REQ_STORAGE = 100

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val uiBarCustom = UiBar(window)
        uiBarCustom.setStatusBarIconColor(isBlack = true)

        requirePermission(PERM_STORAGE, REQ_STORAGE)
        binding.imageButtonBack.setOnClickListener {
            finish()
        }
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
//                Toast.makeText(this, "Strorage permission granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun permissionDenied(requestCode: Int) {
        when(requestCode){
            REQ_STORAGE -> {
                Toast.makeText(this, "Strorage permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun uploadRecipe() {

        currentFocus?.clearFocus()
        binding.parentOfWrite.requestFocus()

        val date = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
        formattedDate = sdf.format(date)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("uid", uid.toString()+formattedDate)
        val email = FirebaseAuth.getInstance().currentUser?.email


        var nickname : String

        val newpost = Post(title = binding.editTextTitle.text.toString(), subtitle = binding.editTextSubtitle.text.toString(), like = 0, comment = 0,
            timestamp = formattedDate, ingredientName = ingredientNameForDB, ingredientAmount = ingredientAmountForDB, recipe = recipeForDB,
            havePhoto = havePhotoList, uid = uid)

        Log.d("newpost", newpost.toString())
        val chunkedUid = uid?.chunked(10)
        val path = chunkedUid!![0]+" "+formattedDate

        db.collection("Post").document(path)
            .set(newpost)
            .addOnSuccessListener {

                //Post에 newpost 등록 성공시
                Log.d("newpost setting successfully", newpost.toString())
                uploadPhoto(photoList, havePhotoList)
                Log.d(TAG, "Upload new recipe successfully")
                //writer 정보 update
                db.collection("Users").document(email.toString()).get()
                    .addOnSuccessListener { document ->
                        val nicknameFromDB = document.data?.get("NickName")
                        nickname = nicknameFromDB.toString()
                        Log.d("get nickname from db", "nickname = $nickname")
                        newpost.writer = nickname
                        Log.d("add nickname in newpost", newpost.writer.toString())

                        db.collection("Post").document(path).update("writer", nickname)
                    }

                //User history posting 정보 update
                db.collection("Users").document(email.toString())
                    .collection("History").document("Posting")
                    .update("posting", FieldValue.arrayUnion(path))
                    .addOnSuccessListener {
                        Log.d("add History posting", "success")
                    }
                    .addOnFailureListener {
                        Log.d("add History posting", "fail")
                    }
                MyApplication.prefs.setPrefs("Posting", "Done")
                MyApplication.prefs.setIntPrefs("postingNum", MyApplication.prefs.getIntPrefs("postingNum", 0)+1)
                val intent = Intent(this, CommunityMainActivity::class.java)
                sleep(2000)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                    e -> Log.d(TAG, "Error writing new recipe", e)
            }





        val sampleComment = CommentForm()
        db.collection("Post").document(path)
            .collection("Comment").add(sampleComment)
            .addOnSuccessListener {
                Log.d("Add Comment collection in Post", "success")
            }
            .addOnFailureListener {
                Log.d("Add Comment collection in Post", "fail")
            }



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

        ingredientNameForDB.add(null)
        ingredientAmountForDB.add(null)

        val ingredientNumber = TextView(this).apply{
            id = countIngredients+1
            text = (countIngredients+1).toString()
            width = 30.dp
            height = 48.dp
            textSize = 6.dp.toFloat()
            setTextColor(ContextCompat.getColor(context!!, R.color.main_green))
            setPadding(10.dp,10.dp,0,0)
        }
        val ingredientName = EditText(this).apply{
            id = countIngredients+1
            hint = "Name"
            width = 100.dp
            height = 48.dp
            setHintTextColor(Color.parseColor("#BCBCBC"))
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(0,10.dp,10.dp,0)
        }
        val ingredientAmount = EditText(this).apply{
            hint = "Amount"
            width = 150.dp
            height = 48.dp
            setHintTextColor(Color.parseColor("#BCBCBC"))
            setBackgroundColor(Color.TRANSPARENT)
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
//                ingredientNameForDB.add(ingredientNumber.id-1, ingredientName.text.toString())
                ingredientNameForDB[ingredientNumber.id-1] = ingredientName.text.toString()
                Log.d("add ingredient name", "at ${ingredientNumber.id-1} ")
                Log.d("ingrenameDB last index","${ingredientNameForDB.lastIndex}")
            }
        }
        ingredientAmount.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
//                ingredientAmountForDB.add(ingredientNumber.id-1, ingredientAmount.text.toString())
                ingredientAmountForDB[ingredientNumber.id-1] = ingredientAmount.text.toString()
                Log.d("add ingredient amount", "at ${ingredientNumber.id-1} ")
                Log.d("ingreAmountDB last index","${ingredientAmountForDB.lastIndex}")
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

        recipeForDB.add(null)
        havePhotoList.add("false")
        photoList.add(null)

        val orderNumber = TextView(this).apply {
            id = countOrder+1
            text = (countOrder+1).toString()
            width = 30.dp
            height = 48.dp
            textSize = 6.dp.toFloat()
            setTextColor(ContextCompat.getColor(context!!, R.color.main_green))
            setPadding(10.dp,10.dp,0,0)
        }
        val orderComment = EditText(this).apply {
            id = countOrder+1
            hint = "Add a comment"
            width = 250.dp
            height = 48.dp
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setHintTextColor(Color.parseColor(("#BCBCBC")))
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(0,10.dp,10.dp,0)
        }
        val orderPhoto = ImageButton(this).apply {
            id = countOrder
            setImageResource(R.drawable.ic_camera_icon)
            scaleType = ImageView.ScaleType.FIT_CENTER
            background = Color.TRANSPARENT.toDrawable()
        }

        orderComment.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
//                recipeForDB.add(orderNumber.id-1, orderComment.text.toString())
                recipeForDB[orderNumber.id-1] = orderComment.text.toString()
                Log.d("add order comment", "at ${orderNumber.id-1} ")
                Log.d("recipeDB last index","${recipeForDB.lastIndex}")
            }
        }

        orderPhoto.setOnClickListener {
            if (checkSelfPermission(PERM_STORAGE[0]) == PackageManager.PERMISSION_GRANTED){
                if (havePhotoList[orderNumber.id] == "true"){
                    Toast.makeText(this, "You already add your photo!", Toast.LENGTH_SHORT).show()
                }
                else{
                    photoNumForOrder = orderNumber.id
                    setPhotoOrder(photoNumForOrder)
                }
            }else{
                Toast.makeText(this, "Strorage permission denied", Toast.LENGTH_SHORT).show()
            }

        }
        orderLayout.addView(orderNumber)
        orderLayout.addView(orderComment)
        orderLayout.addView(orderPhoto, 48.dp, 48.dp)
        countOrder++
    }

    private fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
        if (v.layoutParams is MarginLayoutParams) {
            val p = v.layoutParams as MarginLayoutParams
            p.setMargins(l, t, r, b)
            v.requestLayout()
        }
    }

    //사진 추가가 눌리면 실행되는 함수
    private fun setPhotoOrder(orderNumber : Int) {

        havePhotoList[orderNumber] = "true"
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        Log.d(TAG, "setPhotoOrder putExtra intent : $orderNumber")
        startActivityForResult(intent, 1)

    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap? {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> return bitmap
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1F, 1F)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180F)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180F)
                matrix.postScale(-1F, 1F)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90F)
                matrix.postScale(-1F, 1F)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90F)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90F)
                matrix.postScale(-1F, 1F)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90F)
            else -> return bitmap
        }
        return try {
            val bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
            bmRotated
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            null
        }
    }

    private fun getRealPathFromURI(contentURI: Uri): String? {
        val result: String?
        val cursor: Cursor? = contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) {
            // Source is Dropbox or other similar local file path
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    @SuppressLint("InflateParams")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            when(requestCode){
                1 -> {
                    val currentImageUri : Uri? = data?.data
                    val filepath = Uri.parse(currentImageUri?.let { getRealPathFromURI(it) })

                    Log.d("사진 경로",currentImageUri.toString())

                    try{
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, currentImageUri)

                        var exif: ExifInterface? = null
                        try {
                            exif = filepath.path?.let { ExifInterface(it) }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        val orientation: Int? = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED)

                        Log.d("사진 회전 유무",orientation.toString())

                        val bmRotated = orientation?.let { rotateBitmap(bitmap, it) };

                        Log.d(TAG, intent.toString() )
                        Log.d(TAG, data!!.toString())
//                        val photoNumber = data!!.getIntExtra("num", -1)
//                        Log.d(TAG, "getExtra : $photoNumber")
//                        Log.d(TAG, "getExtra: ${data!!.extras}")
//                        Log.d(TAG, "${data!!.hasExtra("num")}")
//                        if (data!!.hasExtra("num")){
//                            if (photoNumber != -1){
//                                photoList.add(photoNumber, bitmap)
//
//                                photoList.add(bitmap)
//                                val relativeLayout = layoutInflater.inflate(R.layout.photo_order, null) as RelativeLayout
//
//                                val photoNum = relativeLayout.findViewById<View>(R.id.photoNumber) as TextView
//                                photoNum.text = photoNumForOrder.toString()
//
//                                val photo = relativeLayout.findViewById<View>(R.id.photo) as ImageView
//                                photo.setImageBitmap(bitmap)
//                                photo.scaleType = ImageView.ScaleType.FIT_XY
//
//                                val layoutParams = RelativeLayout.LayoutParams(130.dp, 130.dp)
//                                layoutParams.setMargins(0, 0, 5.dp, 5)
//                                relativeLayout.layoutParams = layoutParams
//                                binding.photoOrder.addView(relativeLayout)
//                                Log.d(TAG, "insert photo order")
//
//                                val removePhoto = relativeLayout.findViewById<View>(R.id.removePhoto) as TextView
//                                removePhoto.setOnClickListener {
//                                    binding.photoOrder.removeView(relativeLayout)
//                                }
//                            }
//                            else{
//                                Log.d(TAG, "photoList index error")
//                            }
//                        }


                        val relativeLayout = layoutInflater.inflate(R.layout.photo_order, null) as RelativeLayout

                        val photoNum = relativeLayout.findViewById<View>(R.id.photoNumber) as TextView
                        photoNum.text = photoNumForOrder.toString()

                        photoList[photoNum.text.toString().toInt()-1] = bmRotated
                        val photo = relativeLayout.findViewById<View>(R.id.photo) as ImageView
                        //                        photo.setImageBitmap(bmRotated)
                        photo.clipToOutline = true
                        photo.scaleType = ImageView.ScaleType.CENTER_CROP
                        Glide.with(this).load(currentImageUri).into(photo)
//                        photo.setColorFilter(Color.parseColor("#ffff0000"), PorterDuff.Mode.SRC_IN);
//                        photo.scaleType = ImageView.ScaleType.FIT_XY

                        val layoutParams = RelativeLayout.LayoutParams(150.dp, 150.dp)
                        layoutParams.setMargins(0, 0, 5.dp, 5)
                        relativeLayout.layoutParams = layoutParams
                        binding.photoOrder.addView(relativeLayout)
                        Log.d(TAG, "insert photo order")

                        val removePhoto = relativeLayout.findViewById<View>(R.id.removePhoto) as TextView
                        removePhoto.setOnClickListener {
                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("Remove photo").setMessage("Do you want to remove photo?")
                            builder.setNegativeButton("Remove"){ _, _ ->
                                binding.photoOrder.removeView(relativeLayout)
                                havePhotoList[photoNum.text.toString().toInt()] = "false"
                                photoList[photoNum.text.toString().toInt()-1] = null
                            }
                            builder.setPositiveButton("Cancel", null)
                            builder.create()
                            builder.show()
                        }

                    }
                    catch(e:Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Glide.get(this).clearMemory()
    }

    private fun uploadPhoto(photoList: MutableList<Bitmap?>, havePhotoList: MutableList<String>) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val chunkedUid = uid?.chunked(10)
        var path = chunkedUid!![0]+" "+formattedDate

        val havePhotoIndexList = mutableListOf<Int>()

        Log.d("havePhotoList", havePhotoList.toString())

        for (i in 0 until this.havePhotoList.size){
            if (this.havePhotoList[i] == "true"){
                havePhotoIndexList.add(i)
            }
        }

        Log.d("havePhotoIndexList", havePhotoIndexList.toString())

        for (photo in photoList){
            if (photo != null){
                val index = havePhotoIndexList[0]
                path = "$path $index"
                Log.d("storage path in write activity", path)
                havePhotoIndexList.removeAt(0)
                val nameRef = storageRef.child(path)
                val baos = ByteArrayOutputStream()
                photo.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                val data = baos.toByteArray()

                var uploadTask = nameRef.putBytes(data)
                uploadTask.addOnFailureListener {
                    Log.d("upload photo into storage failed", "")
                }.addOnSuccessListener { taskSnapshot ->
                    // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                    // ...
                    Log.d("upload to storage successfully", "$path $taskSnapshot")
                }
                path = chunkedUid!![0]+" "+formattedDate
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
            havePhotoList.removeAt(havePhotoList.lastIndex)
            countOrder--
        }
    }

    private val Int.dp: Int
        get() {
            val metrics = resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
        }
}