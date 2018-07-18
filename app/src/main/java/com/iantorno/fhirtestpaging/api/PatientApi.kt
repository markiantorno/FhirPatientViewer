package com.iantorno.fhirtestpaging.api

import com.iantorno.fhirtestpaging.objects.PatientResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface PatientApi {

    @GET("Patient")
    fun getPatients(@Query("_count") count: Int): Call<PatientResponse>

    @GET
    fun getPatientsRelational(@Url url: String): Call<PatientResponse>
}