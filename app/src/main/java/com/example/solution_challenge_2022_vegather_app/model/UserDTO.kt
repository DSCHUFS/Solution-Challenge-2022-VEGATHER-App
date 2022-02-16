package com.example.solution_challenge_2022_vegather_app.model

import java.io.Serializable

data class UserDTO(
    var uid: String? = null,
    var email: String? = null,
    var nickName: String? = null,
    var point: Long? = 0,
    var monthlyEat: Long? = 0
) : Serializable