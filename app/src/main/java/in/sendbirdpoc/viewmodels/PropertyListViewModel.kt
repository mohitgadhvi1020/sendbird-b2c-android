package `in`.sendbirdpoc.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.sendbirdpoc.R
import `in`.sendbirdpoc.api.di.ApiModule
import `in`.sendbirdpoc.api.utils.BaseResponse
import `in`.sendbirdpoc.api.utils.SingleLiveEvent
import `in`.sendbirdpoc.api.utils.State
import `in`.sendbirdpoc.model.PropertyListResponse
import `in`.sendbirdpoc.repo.PropertyRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PropertyListViewModel @Inject constructor(
    private val repository: PropertyRepository,
    private val stringResourcesProvider: ApiModule.StringResourcesProvider,
    private val checkConnection: ApiModule.IsInterNetConnectionAvailable
) : ViewModel() {

    private var _propertyListResponse: SingleLiveEvent<State<BaseResponse<PropertyListResponse>>> =
        SingleLiveEvent()
    val propertyListResponse get() = _propertyListResponse

    fun getPropertyList() = viewModelScope.launch {
        safeUpdateProfileApiCall()
    }

    private suspend fun safeUpdateProfileApiCall() {
        _propertyListResponse.value = State.loading()
        try {
            if (checkConnection.isConnected()) {
                val response = repository.getPropertyList()
                _propertyListResponse.value = response
            } else {
                _propertyListResponse.value =
                    State.error(stringResourcesProvider.getString(R.string.no_internet_connection))
            }
        } catch (e: Exception) {
            e.message?.let {
                _propertyListResponse.value = State.error(it)
            }
        }
    }
}