package com.praveenpayasi.simpleimageloaderdemo.core.lrucache

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Collections

class Journal(file: File?, fileManager: FileManager, logger: Logger) {

    private var file: File? = null
    private var fileManager: FileManager? = null
    private var logger: Logger? = null
    private val map: MutableMap<String, Record> = HashMap()
    private var totalSize: Long = 0

    init {
        this.file = file
        this.fileManager = fileManager
        this.logger = logger
    }

    @Throws(IOException::class)
    fun put(record: Record, cacheSize: Long) {
        val fileSize: Long = record.size
        prepare(fileSize, cacheSize)
        put(record)
    }

    private fun put(record: Record) {
        map[record.key!!] = record
        totalSize += record.size
        logger!!.log(
            "[+] Put %s (%d bytes) and cache size became %d bytes",
            record.key,
            record.size,
            totalSize
        )
    }

    fun get(key: String?): Record? {
        val record: Record? = map[key]
        if (record != null) {
            updateTime(record)
            logger!!.log("[^] Update time of %s (%d bytes)", record.key, record.size)
        }
        return record
    }

    fun delete(key: String?): Record? {
        val record = map.remove(key)
        totalSize -= record?.size ?: 0
        return record
    }

    private fun updateTime(record: Record) {
        val time = System.currentTimeMillis()
        map[record.key!!] = Record(record, time)
    }

    @Throws(IOException::class)
    private fun prepare(fileSize: Long, cacheSize: Long) {
        if (totalSize + fileSize > cacheSize) {
            logger!!.log("[!] File %d bytes is not fit in cache %d bytes", fileSize, totalSize)
            val records: ArrayList<Record> = ArrayList(map.values)
            Collections.sort(records, RecordComparator())
            for (c in records.size - 1 downTo 1) {
                val record: Record = records.removeAt(c)
                val nextTotalSize: Long = totalSize - record.size
                logger!!.log(
                    "[x] Delete %s [%d ms] %d bytes and free cache to %d bytes",
                    record.key,
                    record.time,
                    record.size,
                    nextTotalSize
                )
                fileManager!!.delete(record.name)
                map.remove(record.key)
                totalSize = nextTotalSize

                if (totalSize + fileSize <= cacheSize) {
                    break
                }
            }
        }
    }

    private fun setTotalSize(totalSize: Long) {
        this.totalSize = totalSize
    }

    fun writeJournal() {
        try {
            FileOutputStream(file).use { fileStream ->
                DataOutputStream(BufferedOutputStream(fileStream)).use { stream ->
                    stream.writeShort(JOURNAL_FORMAT_VERSION)
                    stream.writeInt(map.size)
                    for (record in map.values) {
                        stream.writeUTF(record.key)
                        stream.writeUTF(record.name)
                        stream.writeLong(record.time)
                        stream.writeLong(record.size)
                    }
                }
            }
        } catch (ex: IOException) {
            logger!!.log("[.] Failed to write journal %s", ex.message)
            ex.printStackTrace()
        }
    }

    companion object {
        const val JOURNAL_FORMAT_VERSION: Int = 1

        fun readJournal(fileManager: FileManager, logger: Logger): Journal {
            val file = fileManager.journal()
            logger.log("[.] Start journal reading", file!!.name)
            val journal = Journal(file, fileManager, logger)
            try {
                FileInputStream(file).use { fileStream ->
                    DataInputStream(BufferedInputStream(fileStream)).use { stream ->
                        val version = stream.readShort().toInt()
                        require(version == JOURNAL_FORMAT_VERSION) { "Invalid journal format version" }
                        val count = stream.readInt()
                        var totalSize: Long = 0
                        for (c in 0 until count) {
                            val key = stream.readUTF()
                            val name = stream.readUTF()
                            val time = stream.readLong()
                            val size = stream.readLong()
                            totalSize += size
                            val record = Record(key, name, time, size)
                            journal.put(record)
                        }
                        journal.setTotalSize(totalSize)
                        logger.log(
                            "[.] Journal read. Files count is %d and total size is %d",
                            count,
                            totalSize
                        )
                    }
                }
            } catch (ignored: FileNotFoundException) {
                logger.log("[.] Journal not found and will be created")
            } catch (ex: IOException) {
                logger.log("[.] Failed to read journal %s", ex.message)
                ex.printStackTrace()
            }
            return journal
        }
    }
}