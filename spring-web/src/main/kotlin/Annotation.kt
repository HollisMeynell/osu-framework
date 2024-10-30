package org.spring.web

//@Target(AnnotationTarget.FIELD)
// 对 body 无效
annotation class Param(
    val name: String = "",
    val notNull: Boolean = true,
    val notEmpty: Boolean = true,
    val min: Long = Long.MIN_VALUE,
    val max: Long = Long.MAX_VALUE,
    val groups: Array<RequestType> = [],
) {
    enum class RequestType
}

fun Param.testType(type: Param.RequestType): Boolean {
    if (groups.isEmpty()) return true
    return type in groups
}

