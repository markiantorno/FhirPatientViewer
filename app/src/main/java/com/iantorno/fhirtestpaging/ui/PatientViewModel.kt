package com.iantorno.fhirtestpaging.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import com.iantorno.fhirtestpaging.repository.NetworkState
import com.iantorno.fhirtestpaging.api.PatientApi
import com.iantorno.fhirtestpaging.objects.Resource
import com.iantorno.fhirtestpaging.repository.paging.PageKeyedPatientSource
import com.iantorno.fhirtestpaging.repository.paging.PatientDataSourceFactory
import io.reactivex.disposables.CompositeDisposable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class PatientViewModel: ViewModel() {

    var patientList: LiveData<PagedList<Resource>>

    private val pageSize = 10

    private val compositeDisposable = CompositeDisposable()

    private val sourceFactory: PatientDataSourceFactory

    init {
        var service = Retrofit.Builder()
                .baseUrl(" http://hapi.fhir.org/baseDstu3/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PatientApi::class.java)

        sourceFactory = PatientDataSourceFactory(service, compositeDisposable)

        val config = PagedList.Config.Builder()
                .setPageSize(pageSize)
                .setInitialLoadSizeHint(pageSize * 2)
                .setEnablePlaceholders(false)
                .build()

        patientList = LivePagedListBuilder<String, Resource>(sourceFactory, config).build()
    }

    fun retry() {
        sourceFactory.sourceLiveData.value!!.retry()
    }

    fun refresh() {
        sourceFactory.sourceLiveData.value!!.invalidate()
    }

    fun getNetworkState(): LiveData<NetworkState> = Transformations.switchMap<PageKeyedPatientSource, NetworkState>(
            sourceFactory.sourceLiveData) { it.networkState }

    fun getRefreshState(): LiveData<NetworkState> = Transformations.switchMap<PageKeyedPatientSource, NetworkState>(
            sourceFactory.sourceLiveData) { it.initialLoad }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}