package com.praveenpayasi.simpleimageloaderdemo.core.lrucache


class RecordComparator : Comparator<Record> {

    companion object {
        private fun compare(x: Long, y: Long): Int {
            return if ((x < y)) -1 else (if ((x == y)) 0 else 1)
        }
    }

    override fun compare(o1: Record, o2: Record): Int {
        return compare(o1.time, o2.time)
    }
}