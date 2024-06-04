package com.praveenpayasi.simpleimageloaderdemo.core

interface ViewHolder<T> {

    fun optSize(): ViewSize?

    fun getSize(): ViewSize

    var tag: Any?

    fun get(): T

}