package com.praveenpayasi.simpleimageloaderdemo.core.lrucache

import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class DiskLruCache private constructor(
    private val fileManager: FileManager,
    private val journal: Journal,
    private val logger: Logger,
    private val cacheSize: Long
) {

    @Throws(IOException::class)
    fun put(key: String, file: File): File {
        synchronized(journal) {
            assertKeyValid(key)
            val name = generateName(key, file)
            val time = System.currentTimeMillis()
            val fileSize = file.length()
            val record = Record(key, name, time, fileSize)
            val cacheFile: File = fileManager.accept(file, name)
            journal.delete(key)
            journal.put(record, cacheSize)
            journal.writeJournal()
            return cacheFile
        }
    }

    operator fun get(key: String?): File? {
        synchronized(journal) {
            assertKeyValid(key)
            val record: Record? = journal.get(key!!)
            if (record != null) {
                var file: File? = fileManager.get(record.name)
                if (!file!!.exists()) {
                    journal.delete(key)
                    file = null
                }
                journal.writeJournal()
                return file
            } else {
                logger.log("[-] No requested file with key %s in cache", key)
                return null
            }
        }
    }

    @Throws(IOException::class, RecordNotFoundException::class)
    fun delete(key: String) {
        delete(key, true)
    }

    @Throws(IOException::class, RecordNotFoundException::class)
    private fun delete(key: String, writeJournal: Boolean) {
        synchronized(journal) {
            assertKeyValid(key)
            val record: Record? = journal.delete(key)
            if (record != null) {
                if (writeJournal) {
                    journal.writeJournal()
                }
                fileManager.delete(record.name)
            } else {
                throw RecordNotFoundException()
            }
        }
    }

    companion object {
        private val UTF_8: Charset = Charset.forName("UTF-8")
        private const val HASH_ALGORITHM: String = "MD5"

        @Throws(IOException::class)
        fun create(cacheDir: File?, cacheSize: Long): DiskLruCache {
            val fileManager: FileManager = SimpleFileManager(cacheDir)
            val logger: Logger = SimpleLogger(false)
            return create(fileManager, logger, cacheSize)
        }

        @Throws(IOException::class)
        fun create(fileManager: FileManager, logger: Logger, cacheSize: Long): DiskLruCache {
            fileManager.prepare()
            val journal: Journal = Journal.readJournal(fileManager, logger)
            return DiskLruCache(fileManager, journal, logger, cacheSize)
        }

        private fun assertKeyValid(key: String?) {
            require(!key.isNullOrEmpty()) {
                String.format(
                    "Invalid key value: '%s'", key
                )
            }
        }

        private fun keyHash(base: String): String {
            try {
                val digest = MessageDigest.getInstance(HASH_ALGORITHM)
                val bytes = digest.digest(base.toByteArray(UTF_8))
                val hexString = StringBuilder()
                for (b in bytes) {
                    val hex = Integer.toHexString(0xff and b.toInt())
                    if (hex.length == 1) {
                        hexString.append('0')
                    }
                    hexString.append(hex)
                }
                return hexString.toString()
            } catch (ignored: NoSuchAlgorithmException) {
            }
            throw IllegalArgumentException("Unable to hash key")
        }

        private fun generateName(key: String, file: File): String {
            return keyHash(key) + fileExtension(file.name)
        }

        private fun fileExtension(path: String?): String {
            var suffix = ""
            if (!path.isNullOrEmpty()) {
                val index = path.lastIndexOf(".")
                if (index != -1) {
                    suffix = path.substring(index)
                }
            }
            return suffix
        }
    }
}
