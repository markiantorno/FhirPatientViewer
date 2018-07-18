package com.iantorno.fhirtestpaging.repository.paging

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import com.iantorno.fhirtestpaging.api.PatientApi
import com.iantorno.fhirtestpaging.objects.Resource
import java.util.concurrent.Executor

class SubRedditDataSourceFactory(
        private val patientApi: PatientApi,
        private val retryExecutor: Executor) : DataSource.Factory<String, Resource>() {

    private val sourceLiveData = MutableLiveData<PageKeyedPatientSource>()

    override fun create(): DataSource<String, Resource> {
        val source = PageKeyedPatientSource(patientApi, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}