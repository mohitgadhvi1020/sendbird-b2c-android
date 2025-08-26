package `in`.sendbirdpoc.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.sendbirdpoc.R
import `in`.sendbirdpoc.api.di.ApiModule
import `in`.sendbirdpoc.api.utils.BaseResponse
import `in`.sendbirdpoc.api.utils.SingleLiveEvent
import `in`.sendbirdpoc.api.utils.State
import `in`.sendbirdpoc.model.LoginResponse
import `in`.sendbirdpoc.repo.PropertyRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: PropertyRepository,
    private val stringResourcesProvider: ApiModule.StringResourcesProvider,
    private val checkConnection: ApiModule.IsInterNetConnectionAvailable
) : ViewModel() {

    private var _loginResponse: SingleLiveEvent<State<BaseResponse<LoginResponse>>> =
        SingleLiveEvent()
    val loginResponse get() = _loginResponse

    fun login(request: MutableMap<String, String>) = viewModelScope.launch {
        safeUpdateProfileApiCall(request)
    }

    private suspend fun safeUpdateProfileApiCall(request: MutableMap<String, String>) {
        _loginResponse.value = State.loading()
        try {
            if (checkConnection.isConnected()) {
                val response = repository.login(request)
                _loginResponse.value = response
            } else {
                _loginResponse.value =
                    State.error(stringResourcesProvider.getString(R.string.no_internet_connection))
            }
        } catch (e: Exception) {
            e.message?.let {
                _loginResponse.value = State.error(it)
            }
        }
    }
}