package org.spring.web

data class DataVo<T>(
    var code: Int = 200,
    var message: String = "ok",
    var data: T? = null
)

data class DataListVo<T>(
    var code: Int = 200,
    var message: String = "ok",
    var data: List<T>? = emptyList(),
    var pageSize: Int = 10,
    var totalPages: Int = 1,
    var totalItems: Int = 1,
    var currentPage: Int = 1,
)