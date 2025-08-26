package `in`.sendbirdpoc.api.utils

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(

    /*@SerializedName("status")
    @Expose
    var status: Int,
    @SerializedName("http_code")
    @Expose
    var http_code: Int,
    @SerializedName("message")
    @Expose
    var message: String,*/

    @SerializedName("success")
    @Expose
    var success: Boolean,
    @SerializedName("data")
    @Expose
    var data: T
)
