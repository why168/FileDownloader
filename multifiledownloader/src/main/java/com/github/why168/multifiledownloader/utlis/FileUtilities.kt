package com.github.why168.multifiledownloader.utlis

import android.os.Environment

import com.github.why168.multifiledownloader.Constants

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DecimalFormat

/**
 * @author Edwin.Wu
 * @version 2017/1/4 18:09
 * @since JDK1.8
 */
object FileUtilities {

    private const val HASH_ALGORITHM = "MD5"
    private const val RADIX = 10 + 26 // 10 digits + 26 letters

    fun getMd5FileName(url: String): String {
        val md5 = getMD5(url.toByteArray())
        val bi = BigInteger(md5).abs()
        return bi.toString(RADIX) + url.substring(url.lastIndexOf("/") + 1)
    }

    private fun getMD5(data: ByteArray): ByteArray? {
        var hash: ByteArray? = null
        try {
            val digest = MessageDigest.getInstance(HASH_ALGORITHM)
            digest.update(data)
            hash = digest.digest()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return hash
    }


    @Synchronized
    fun getDownloadFile(url: String): File {
        val file = File(Constants.PATH_BASE)
        if (!file.exists()) {
            file.mkdirs()
        }

        return File(file, getMd5FileName(url))
    }


    fun delFile(file: File): Boolean {
        if (!file.exists()) {
            return false
        }
        if (file.isDirectory) {
            val files = file.listFiles()
            for (f in files) {
                delFile(f)
            }
        }
        return file.delete()
    }


    fun clearFileDownloader(): Boolean {
        return delFile(File(Constants.PATH_BASE))
    }

    /**
     * 转换文件大小
     *
     * @param fileSize 文件大小
     * @return 格式化
     */
    fun convertFileSize(fileSize: Long): String {
        if (fileSize <= 0) {
            return "0M"
        }
        val df = DecimalFormat("#.00")
        val fileSizeString: String
        fileSizeString = when {
            fileSize < 1024 -> df.format(fileSize.toDouble()) + "B"
            fileSize < 1024 * 1024 -> df.format(fileSize.toDouble() / 1024) + "K"
            fileSize < 1024 * 1024 * 1024 -> df.format(fileSize.toDouble() / (1024 * 1024)) + "M"
            else -> df.format(fileSize.toDouble() / (1024 * 1024 * 1024)) + "G"
        }
        return fileSizeString
    }
}
