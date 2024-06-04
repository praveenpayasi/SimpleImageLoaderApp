package com.praveenpayasi.simpleimageloaderdemo.core

import android.graphics.drawable.Drawable

interface Result {

    fun getByteCount(): Int

    fun isRecycled(): Boolean

    fun getDrawable(): Drawable

}