package `in`.sendbirdpoc.api.utils

import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class BaseService @Inject constructor() {

    protected suspend fun <T : Any>
            createCall(
        call: suspend () -> Response<T>
    ): State<T> {
        val response: Response<T>
        try {
            response = call.invoke()
            /**
             *   UnAuthorized 401
             */
            return if (response.isSuccessful && response.body() != null) {
                with(response.body() as BaseResponse<*>) {
                    if (success) {
                        State.Companion.success(response.body()!!)
                    } else {
                        State.Companion.error("", success)
                    }
                }
            } else {

                with(response.body() as BaseResponse<*>) {
                    val errorBody = response.errorBody()
                    if (errorBody != null) {
                        State.Companion.error(parseError(errorBody), this.success)
                    } else {
                        State.Companion.error(mapApiException(response.code()), this.success)
                    }
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            return State.Companion.error(mapToNetworkError(t), false)
        }
    }

    private fun parseError(errorBody: ResponseBody?): String {

        if (errorBody != null) {

            val jsonObject = JSONObject(errorBody.string())

            return jsonObject.getString("message")
        }

        return ""
    }

    private fun mapApiException(code: Int): String {
        return when (code) {
            HttpURLConnection.HTTP_NOT_FOUND -> "Not found"
            HttpURLConnection.HTTP_UNAUTHORIZED -> "UnAuthorized"
            else -> "Something went wrong"
        }
    }

    private fun mapToNetworkError(t: Throwable): String {
        return when (t) {
            is SocketTimeoutException
                -> "Connection Timed Out"

            is UnknownHostException
                -> "No internet connection available."

            else
                -> "No internet connection available."
        }
    }
}