package com.praveenpayasi.simpleimageloaderdemo.util.loader

import android.content.res.AssetManager
import com.praveenpayasi.simpleimageloaderdemo.core.Loader
import com.praveenpayasi.simpleimageloaderdemo.util.safeClose
import com.praveenpayasi.simpleimageloaderdemo.util.safeCopyTo
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URI

class FileLoader(private val assets: AssetManager?) : Loader {

    override val schemes: List<String>
        get() = listOf("file")

    override fun load(uriString: String, file: File): Boolean {
        val uri = URI(uriString)
        var input: InputStream? = null
        var output: OutputStream? = null
        try {
            input = if (uri.path.startsWith(ASSET_PREFIX)) {
                assets?.open(uri.path.replace(ASSET_PREFIX, ""))
            } else {
                val sourceFile = File(uri)
                FileInputStream(sourceFile)
            }
            output = FileOutputStream(file)
            return input?.safeCopyTo(output) ?: false
        } finally {
            input?.safeClose()
            output?.safeClose()
        }
    }

}

private const val ASSET_PREFIX = "/android_asset/"
