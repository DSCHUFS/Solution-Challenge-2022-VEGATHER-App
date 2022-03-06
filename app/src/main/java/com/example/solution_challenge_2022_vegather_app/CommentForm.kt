package com.example.solution_challenge_2022_vegather_app

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommentForm(
    var documentId : String? = null,
    val useremail : String? = null,
    val nickname : String? = null,
    val text : String? = null,
    val timestamp : String? = null,
    var like : HashMap<String,Boolean> = HashMap(),
    var reply : Int? = null
) : Parcelable
