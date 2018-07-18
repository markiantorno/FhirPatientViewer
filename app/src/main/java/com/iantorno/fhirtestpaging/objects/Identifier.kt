package com.iantorno.fhirtestpaging.objects

data class Identifier(
    val use: String,
    val type: Type,
    val value: String,
    val assigner: Assigner
)