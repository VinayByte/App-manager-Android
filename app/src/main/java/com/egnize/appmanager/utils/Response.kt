package com.egnize.chineseapps.utils

import com.google.gson.annotations.SerializedName

data class Response(

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("pkg")
    val pkg: String? = null
)
