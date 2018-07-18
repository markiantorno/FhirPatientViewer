package com.iantorno.fhirtestpaging.objects

data class Entry(
    val fullUrl: String,
    val resource: Resource,
    val search: Search
)