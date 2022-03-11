package com.example.solution_challenge_2022_vegather_app

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryCommentRecipe(
    val basicComment: HashMap<String,Int> = HashMap(),
    val communityComment : HashMap<String,Int> = HashMap()
) : Parcelable
