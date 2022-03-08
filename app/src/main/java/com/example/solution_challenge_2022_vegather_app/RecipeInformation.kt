package com.example.solution_challenge_2022_vegather_app

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecipeInformation(
    var like : Int = 0,
    var comment : Int = 0,
    var searched : Int = 0,
    var name : String = "None",
    var introduce : String = "None",
    var imgUrl : String? = null,
    var nutrition : ArrayList<String> = ArrayList<String>(),
    var ingredient : ArrayList<String> = ArrayList<String>(),
    var order : ArrayList<String> = ArrayList<String>()
) : Parcelable
