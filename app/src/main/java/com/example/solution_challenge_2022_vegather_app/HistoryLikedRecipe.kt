package com.example.solution_challenge_2022_vegather_app

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryLikedRecipe(
    val basicRecipe: ArrayList<String> = ArrayList<String>(),
    val communityRecipe : ArrayList<String> = ArrayList<String>()
    ) : Parcelable{
    fun size(): Int {
        return basicRecipe.size + communityRecipe.size
    }
    }
