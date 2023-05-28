package com.watso.app.API

import com.google.gson.annotations.SerializedName

/** Auth API 관련 데이터 모델 */

data class LoginModel(
    val username: String,
    val pw: String
)

data class LoginKey(
    @SerializedName("key")
    val loginKey: String
)