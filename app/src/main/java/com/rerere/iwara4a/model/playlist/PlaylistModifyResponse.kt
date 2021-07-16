package com.rerere.iwara4a.model.playlist


import com.google.gson.annotations.SerializedName

data class PlaylistModifyResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
)