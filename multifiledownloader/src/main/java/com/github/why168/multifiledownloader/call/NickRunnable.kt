package com.github.why168.multifiledownloader.call

import android.os.Process
import java.util.*

/**
 * NickRunnable
 *
 * @author Edwin.Wu
 * @version 2017/6/13 16:15
 * @since JDK1.8
 */
abstract class NickRunnable internal constructor(format: String = "", args: String) : Runnable {
    protected val name: String

    init {
        name = String.format(Locale.US, format, args)
    }

    override fun run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
        val oldName = Thread.currentThread().name
        Thread.currentThread().name = "$name --- $oldName"
        try {
            execute()
        } finally {
            Thread.currentThread().name = oldName
        }
    }

    protected abstract fun execute()

}