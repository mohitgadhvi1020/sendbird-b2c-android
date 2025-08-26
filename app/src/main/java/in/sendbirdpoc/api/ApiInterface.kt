package `in`.sendbirdpoc.api

import `in`.sendbirdpoc.api.utils.BaseResponse
import `in`.sendbirdpoc.model.LoginResponse
import `in`.sendbirdpoc.model.PropertyListResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiInterface {

    @GET("api/property/listings")
    suspend fun getPropertyList(
    ): Response<BaseResponse<PropertyListResponse>>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: MutableMap<String, String>
    ): Response<BaseResponse<LoginResponse>>

    @PUT("group_channels/{channel_url}/leave")
    fun removeUser(
        @Header("Api-Token") token: String,
        @Path("channel_url") channelUrl: String,
        @Body request: MutableMap<String, Any>
    ): Call<Unit>

    @POST("users/{user_id}/block")
    fun blockUser(
        @Header("Api-Token") token: String,
        @Path("user_id") userId: String,
        @Body request: MutableMap<String, Any>
    ): Call<Unit>

    @DELETE("users/{user_id}/block/{unblock_user_id}")
    fun unBlockUser(
        @Header("Api-Token") token: String,
        @Path("user_id") userId: String,
        @Path("unblock_user_id") unblockUserId: String
    ): Call<Unit>

    @POST("group_channels/{channel_url}/mute")
    fun muteUser(
        @Header("Api-Token") token: String,
        @Path("channel_url") channelUrl: String,
        @Body request: MutableMap<String, Any>
    ): Call<Unit>

    @DELETE("group_channels/{channel_url}/mute/{user_id}")
    fun unmuteUSer(
        @Header("Api-Token") token: String,
        @Path("channel_url") channelUrl: String,
        @Path("user_id") userId: String
    ): Call<Unit>
}

