package com.example.solution_challenge_2022_vegather_app

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.solution_challenge_2022_vegather_app.databinding.MainPageMoreRecipeRecyclerBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MoreRecipeAdapter(private val binding : MainPageMoreRecipeRecyclerBinding) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var dataset = ArrayList<RecipeInformation>()
    private lateinit var context : Context

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storageRef : StorageReference = FirebaseStorage.getInstance().reference

    inner class MoreRecipeViewHolder(val binding : MainPageMoreRecipeRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    = MoreRecipeViewHolder(MainPageMoreRecipeRecyclerBinding
        .inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as MoreRecipeViewHolder).binding

        Glide.with(context)
            .load(R.drawable.loading_bigsize)
            .into(binding.imageView3)

        binding.foodName.text = dataset[position].name
        binding.foodInfo.text = dataset[position].introduce
        binding.likeCount.text = dataset[position].like.toString()
        val imgRef = storageRef.child(dataset[position].imgUrl.toString())

        imgRef.downloadUrl.addOnSuccessListener {
            Glide.with(context)
                .load(it)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.imageView3)
        }

        if (position <= dataset.size) {
            val endPosition = if (position + 3 > dataset.size) {
                dataset.size
            } else {
                position + 3
            }
            dataset.subList(position, endPosition ).map { it.imgUrl }.forEach {
                preload(context, it.toString())
            }
        }


        binding.container.setOnClickListener {
            goToRecipePage(position)
        }

        db.collection("Recipe").document(dataset[position].name)
            .addSnapshotListener { value, error ->
                val recipeInfo = value?.toObject(RecipeInformation::class.java)
                binding.likeCount.text = recipeInfo?.like.toString()
            }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun setData(recipeData : ArrayList<RecipeInformation>){
        dataset.clear()
        for( recipe in recipeData){
            dataset.add(recipe)
        }
    }

    private fun getRecipeImageUrl( recipeName : String ) : String {
        return "Recipe/${recipeName}.jpg"
    }

    private fun getImage( url : String, Img : ImageView ){
        val imgRef = storageRef.child(url)
        imgRef.downloadUrl.addOnSuccessListener {
            Glide.with(context)
                .load(it)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(Img)
        }
    }

    fun loadParentActivity( c : Context){
        context = c
    }

    private fun goToRecipePage( position: Int ){
        val intentRecipe = Intent(context,RecipeMainActivity::class.java)
        intentRecipe.putExtra("recipeInfo",dataset[position])
        context.startActivity(intentRecipe)
    }

    fun preload(context: Context,  url : String) {
        Glide.with(context)
            .load(url)
            .centerCrop()
            .preload()
    }
}