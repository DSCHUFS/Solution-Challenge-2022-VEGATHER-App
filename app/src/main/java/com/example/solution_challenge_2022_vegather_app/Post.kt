package com.example.solution_challenge_2022_vegather_app

data class Post(
    var title:Any?,
    var subtitle:Any?,
    var writer:Any? = "",
    var ingredientName : MutableList<Any?> = mutableListOf(""),
    var ingredientAmount  : MutableList<Any?> = mutableListOf(""),
    var recipe  : MutableList<Any?> = mutableListOf(""),
    var timestamp:Any?,
    var like:Int? = 0,
    var comment:Int? = 0

)