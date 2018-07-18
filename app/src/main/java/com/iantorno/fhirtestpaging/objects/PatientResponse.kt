package com.iantorno.fhirtestpaging.objects

data class PatientResponse(
    val resourceType: String,
    val id: String,
    val meta: Meta,
    val type: String,
    val total: Int,
    val link: List<Link>,
    val entry: List<Entry>
)