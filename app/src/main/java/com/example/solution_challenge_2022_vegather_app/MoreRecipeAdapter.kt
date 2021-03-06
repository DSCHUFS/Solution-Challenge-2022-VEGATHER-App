package com.example.solution_challenge_2022_vegather_app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.solution_challenge_2022_vegather_app.databinding.MainPageMoreRecipeRecyclerBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.protobuf.NullValue

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
        val activity : Activity = context as Activity
        if(activity.isFinishing) return;
        binding.imageView3.visibility = View.INVISIBLE

        binding.foodName.text = dataset[position].name
        binding.foodInfo.text = dataset[position].introduce
        binding.likeCount.text = dataset[position].like.toString()
        val imgRef = storageRef.child(dataset[position].imgUrl.toString())

        Log.d("MoreRecipeImageSize", binding.imageView3.width.toString() + " " + binding.imageView3.height.toString())

        imgRef.downloadUrl.addOnSuccessListener {
            binding.imageView3.visibility = View.VISIBLE

            if(!activity.isFinishing){
                Glide.with(context)
                    .load(it)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .into(binding.imageView3)
            }
        }

        if (position <= dataset.size) {
            val endPosition = if (position + 5 > dataset.size) {
                dataset.size
            } else {
                position + 5
            }
            dataset.subList(position, endPosition ).map { it.imgUrl }.forEach {
                preload(context, it.toString())
            }
        }

        binding.container.setOnClickListener {
            goToRecipePage(position)
            updateSearchCount(dataset[position])
        }

        db.collection("Recipe").document(dataset[position].name)
            .addSnapshotListener { value, error ->
                val recipeInfo = value?.toObject(RecipeInformation::class.java)
                if( recipeInfo!=null ){
                    binding.likeCount.text = recipeInfo.like.toString()
                }
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

    private fun updateSearchCount( recipe : RecipeInformation ){
        db.collection("Recipe").document(recipe.name)
            .update("searched",FieldValue.increment(1))
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