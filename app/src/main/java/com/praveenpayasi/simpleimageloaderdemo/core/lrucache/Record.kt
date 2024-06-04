package com.praveenpayasi.simpleimageloaderdemo.core.lrucache

data class Record(
    val key: String?, val name: String?, val time: Long, val size: Long
) {
    constructor(record: Record, time: Long) : this(record.key, record.name, time, record.size)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        other as Record

        if (time != other.time) return false
        if (size != other.size) return false
        if (key != other.key) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (time xor (time ushr 32)).toInt()
        result = 31 * result + (size xor (size ushr 32)).toInt()
        return result
    }
}
