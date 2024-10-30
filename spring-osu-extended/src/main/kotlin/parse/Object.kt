package org.spring.osu.extended.parse

import kotlin.math.sqrt

data class Point(
    var x: Float,
    var y: Float,
) {
    fun length(): Float {
        return sqrt(x * x + y * y)
    }
    infix fun midPoint(other: Point): Point {
        return Point((x + other.x) / 2, (y + other.y) / 2)
    }

    operator fun plusAssign(other: Point) {
        x += other.x
        y += other.y
    }

    operator fun minusAssign(other: Point) {
        x += other.x
        y += other.y
    }

    operator fun timesAssign(other: Float) {
        x *= other
        y *= other
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Point) return false
        return x == other.x && y == other.y
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}