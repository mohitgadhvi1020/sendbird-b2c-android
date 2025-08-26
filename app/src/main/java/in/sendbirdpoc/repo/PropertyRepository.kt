package `in`.sendbirdpoc.repo

import `in`.sendbirdpoc.api.ApiInterface
import `in`.sendbirdpoc.api.utils.BaseService
import `in`.sendbirdpoc.data.AppSharedPref
import javax.inject.Inject

class PropertyRepository @Inject constructor(
    val api: ApiInterface,
    private val prefManager: AppSharedPref
) : BaseService() {

    suspend fun getPropertyList() =
        createCall { api.getPropertyList() }

    suspend fun login(request: MutableMap<String, String>) =
        createCall { api.login(request) }
}
