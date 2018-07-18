package com.iantorno.fhirtestpaging.objects

data class Resource(
    val resourceType: String,
    val id: String,
    val meta: Meta,
    val text: Text,
    val identifier: List<Identifier>,
    val active: Boolean,
    val gender: String,
    val birthDate: String,
    val deceasedBoolean: Boolean
)