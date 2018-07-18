package com.iantorno.fhirtestpaging.repository.paging

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import com.iantorno.fhirtestpaging.api.PatientApi
import com.iantorno.fhirtestpaging.objects.Resource
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.Executor

class PatientDataSourceFactory(
        private val patientApi: PatientApi,
        private val compositeDisposable: CompositeDisposable)
    : DataSource.Factory<String, Resource>() {

    val sourceLiveData = MutableLiveData<PageKeyedPatientSource>()

    override fun create(): DataSource<String, Resource> {
        val source = PageKeyedPatientSource(patientApi, compositeDisposable)
        sourceLiveData.postValue(source)
        return source
    }
}