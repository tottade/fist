package com.lixiaoyun.aike.network

import com.google.gson.annotations.SerializedName

class BaseResult<T> {

    @SerializedName(value = "code", alternate = ["otherCodeName"])
    var code: Int? = null

    @SerializedName(value = "data", alternate = ["otherDataName"])
    var data: T? = null

    @SerializedName(value = "message", alternate = ["otherMessageName"])
    var message: String? = null

    @SerializedName(value = "error", alternate = ["otherErrorName"])
    var error: String? = null
}
