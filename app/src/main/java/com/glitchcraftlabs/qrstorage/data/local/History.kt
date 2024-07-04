package com.glitchcraftlabs.qrstorage.data.local

data class History(

    var tag: String? = null,

    var createdAt: Long? = null,

    var data: String? = null,

    var isGenerated: Boolean? = null,

    var isFile: Boolean? = null
)