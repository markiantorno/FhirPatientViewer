package com.iantorno.fhirtestpaging.repository.paging

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import com.android.example.paging.pagingwithnetwork.reddit.repository.NetworkState
import com.iantorno.fhirtestpaging.api.PatientApi
import com.iantorno.fhirtestpaging.objects.LinkType.*
import com.iantorno.fhirtestpaging.objects.PatientResponse
import com.iantorno.fhirtestpaging.objects.Resource
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.Executor

class PageKeyedPatientSource(
        private val patientApi: PatientApi,
        private val retryExecutor: Executor) : PageKeyedDataSource<String, Resource>() {


    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState = MutableLiveData<NetworkState>()
    val initialLoad = MutableLiveData<NetworkState>()

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, Resource>) {
        // Ignored.
    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, Resource>) {
        val request = patientApi.getPatients(
                count = params.requestedLoadSize
        )
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        // triggered by a refresh, we better execute sync
        try {
            val response = request.execute()
            val data = response.body()
            val items = data?.entry?.map { it.resource } ?: emptyList()

            retry = null
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
            callback.onResult(items,
                    data?.link?.first { return@first it.relation == PREVIOUS.label }?.url,
                    data?.link?.first { return@first it.relation == NEXT.label }?.url)
        } catch (ioException: IOException) {
            retry = {
                loadInitial(params, callback)
            }
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Resource>) {
        networkState.postValue(NetworkState.LOADING)
        patientApi.getPatientsRelational(url = params.key).enqueue(
                object : retrofit2.Callback<PatientResponse> {
                    override fun onFailure(call: Call<PatientResponse>, t: Throwable) {
                        retry = {
                            loadAfter(params, callback)
                        }
                        networkState.postValue(NetworkState.error(t.message ?: "unknown err"))
                    }

                    override fun onResponse(
                            call: Call<PatientResponse>,
                            response: Response<PatientResponse>) {
                        if (response.isSuccessful) {
                            val data = response.body()
                            val items = data?.entry?.map { it.resource } ?: emptyList()
                            retry = null
                            callback.onResult(items,
                                    data?.link?.first { return@first it.relation == NEXT.label }?.url)
                            networkState.postValue(NetworkState.LOADED)
                        } else {
                            retry = {
                                loadAfter(params, callback)
                            }
                            networkState.postValue(
                                    NetworkState.error("error code: ${response.code()}"))
                        }
                    }
                }
        )    }
}