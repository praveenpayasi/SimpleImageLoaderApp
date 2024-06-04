package com.praveenpayasi.simpleimageloaderdemo.core

import java.io.File

interface Loader {

    val schemes: List<String>

    fun load(uriString: String, file: File): Boolean

}
