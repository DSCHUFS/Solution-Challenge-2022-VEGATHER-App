package com.example.solution_challenge_2022_vegather_app

data class Post(
    var title:String,
    var subtitle:String,
    var writer:String = "",
    var ingredient:String = "",
    var recipe:String = "",
    var timestamp:Long,
    var like:Int = 0,
    var comment:Int = 0

)