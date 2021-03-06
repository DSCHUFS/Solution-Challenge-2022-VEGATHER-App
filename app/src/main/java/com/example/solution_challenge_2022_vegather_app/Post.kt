package com.example.solution_challenge_2022_vegather_app

import com.facebook.internal.Mutable

data class Post(
    var title:Any?,
    var subtitle:Any?,
    var writer:String? = "",
    var ingredientName : MutableList<Any?> = mutableListOf(""),
    var ingredientAmount  : MutableList<Any?> = mutableListOf(""),
    var recipe  : MutableList<Any?> = mutableListOf(""),
    var timestamp:Any?,
    var like:Int? = 0,
    var comment:Int? = 0,
    var havePhoto: MutableList<String> = mutableListOf("false"),
    var uid:String? = ""
)