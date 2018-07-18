package com.iantorno.fhirtestpaging.objects

enum class LinkType(val label: String) {
    SELF("self"),
    NEXT("next"),
    PREVIOUS("previous");

    companion object {
        private val map = LinkType.values().associateBy(LinkType::label)
        fun fromLabel(type: String) = map[type]
    }
}