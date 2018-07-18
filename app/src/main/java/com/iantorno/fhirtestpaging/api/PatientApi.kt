package com.iantorno.fhirtestpaging.api

import com.iantorno.fhirtestpaging.objects.PatientResponse
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface PatientApi {

    @GET("Patient")
    fun getPatients(@Query("_count") count: Int): Single<PatientResponse>

    @GET
    fun getPatientsRelational(@Url url: String): Single<PatientResponse>
}