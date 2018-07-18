package com.iantorno.fhirtestpaging.repository.paging

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import com.iantorno.fhirtestpaging.repository.NetworkState
import com.iantorno.fhirtestpaging.api.PatientApi
import com.iantorno.fhirtestpaging.objects.LinkType.NEXT
import com.iantorno.fhirtestpaging.objects.LinkType.PREVIOUS
import com.iantorno.fhirtestpaging.objects.Resource
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class PageKeyedPatientSource(
        private val patientApi: PatientApi,
        private val compositeDisposable: CompositeDisposable)
    : PageKeyedDataSource<String, Resource>() {

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState = MutableLiveData<NetworkState>()
    val initialLoad = MutableLiveData<NetworkState>()

    /**
     * Keep Completable reference for the retry event
     */
    private var retryCompletable: Completable? = null

    fun retry() {
        if (retryCompletable != null) {
            compositeDisposable.add(retryCompletable!!
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ }, { throwable -> Timber.e(throwable.message) }))
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, Resource>) {
        // Ignored.
    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<String, Resource>) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        compositeDisposable.add(patientApi.getPatients(count = params.requestedLoadSize).subscribe({ response ->
            setRetry(null)
            val items = response?.entry?.map { it.resource } ?: emptyList()
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
            callback.onResult(items,
                    response?.link?.find { it.relation == PREVIOUS.label }?.url ?: "",
                    response?.link?.find { it.relation == NEXT.label }?.url ?: "")
        }, { throwable ->
            setRetry(Action {
                loadInitial(params, callback)
            })
            val error = NetworkState.error(throwable.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }))
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Resource>) {
        networkState.postValue(NetworkState.LOADING)

        compositeDisposable.add(patientApi.getPatientsRelational(url = params.key).subscribe({ response ->
            setRetry(null)
            val items = response?.entry?.map { it.resource } ?: emptyList()
            networkState.postValue(NetworkState.LOADED)
            callback.onResult(items,
                    response?.link?.find { it.relation == NEXT.label }?.url)
        }, { throwable ->
            setRetry(Action {
                loadAfter(params, callback)
            })
            val error = NetworkState.error(throwable.message ?: "unknown error")
            networkState.postValue(error)
        }))
    }

    private fun setRetry(action: Action?) {
        if (action == null) {
            this.retryCompletable = null
        } else {
            this.retryCompletable = Completable.fromAction(action)
        }
    }
}