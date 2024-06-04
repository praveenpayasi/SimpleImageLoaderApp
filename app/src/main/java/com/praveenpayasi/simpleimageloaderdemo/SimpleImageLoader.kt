package com.praveenpayasi.simpleimageloaderdemo

import android.content.Context
import com.praveenpayasi.simpleimageloaderdemo.core.Decoder
import com.praveenpayasi.simpleimageloaderdemo.core.DiskCacheImpl
import com.praveenpayasi.simpleimageloaderdemo.core.FileProvider
import com.praveenpayasi.simpleimageloaderdemo.core.FileProviderImpl
import com.praveenpayasi.simpleimageloaderdemo.core.ImageLoader
import com.praveenpayasi.simpleimageloaderdemo.core.ImageLoaderImpl
import com.praveenpayasi.simpleimageloaderdemo.core.MainExecutorImpl
import com.praveenpayasi.simpleimageloaderdemo.core.MemoryCache
import com.praveenpayasi.simpleimageloaderdemo.core.MemoryCacheImpl
import com.praveenpayasi.simpleimageloaderdemo.core.lrucache.DiskLruCache
import com.praveenpayasi.simpleimageloaderdemo.util.BitmapDecoder
import com.praveenpayasi.simpleimageloaderdemo.util.loader.ContentLoader
import com.praveenpayasi.simpleimageloaderdemo.util.loader.FileLoader
import com.praveenpayasi.simpleimageloaderdemo.util.loader.UrlLoader
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object SimpleImageLoader {

    private var imageLoader: ImageLoader? = null

    fun Context.imageLoader(): ImageLoader {
        return imageLoader ?: initImageLoader()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun Context.initImageLoader(
        decoders: List<Decoder> = listOf(BitmapDecoder()),
        fileProvider: FileProvider = FileProviderImpl(
            cacheDir,
            DiskCacheImpl(DiskLruCache.create(cacheDir, 15728640L)),
            UrlLoader(),
            FileLoader(assets),
            ContentLoader(contentResolver)
        ),
        memoryCache: MemoryCache = MemoryCacheImpl(),
        mainExecutor: Executor = MainExecutorImpl(),
        backgroundExecutor: ExecutorService = Executors.newFixedThreadPool(10)
    ): ImageLoader {
        val loader = ImageLoaderImpl(
            fileProvider, decoders, memoryCache, mainExecutor, backgroundExecutor
        )
        imageLoader = loader
        return loader
    }

}