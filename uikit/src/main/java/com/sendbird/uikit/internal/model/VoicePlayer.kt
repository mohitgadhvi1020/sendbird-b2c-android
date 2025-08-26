package com.sendbird.uikit.internal.model

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.message.FileMessage
import com.sendbird.uikit.interfaces.OnResultHandler
import com.sendbird.uikit.internal.extensions.runOnUiThread
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.utils.ClearableScheduledExecutorService
import com.sendbird.uikit.utils.FileUtils
import com.sendbird.uikit.vm.FileDownloader
import java.io.File
import java.util.concurrent.TimeUnit

internal class VoicePlayer(val key: String) {
    enum class Status {
        STOPPED, PREPARING, PLAYING, PAUSED
    }

    interface OnUpdateListener {
        @UiThread
        fun onUpdated(key: String, status: Status)
    }

    interface OnProgressUpdateListener {
        @UiThread
        fun onProgressUpdated(key: String, status: Status, milliseconds: Int, duration: Int)
    }

    private val onUpdateListenerSet: MutableSet<OnUpdateListener> = hashSetOf()
    private val onProgressUpdateListenerSet: MutableSet<OnProgressUpdateListener> = hashSetOf()
    var duration: Int = 0
    var status: Status = Status.STOPPED
        private set

    private val player: MediaPlayer = MediaPlayer()
    private val progressExecutor by lazy { ClearableScheduledExecutorService() }
    private val uiThreadHandler by lazy { Handler(Looper.getMainLooper()) }

    @UiThread
    @Synchronized
    fun play(
        context: Context,
        message: FileMessage,
        duration: Int,
        onUpdateListener: OnUpdateListener,
        onProgressUpdateListener: OnProgressUpdateListener
    ) {
        Logger.i("VoicePlayer::play()")
        val voiceFile: File? = getData(context, message)
        if (voiceFile != null) {
            play(context, voiceFile, duration, onUpdateListener, onProgressUpdateListener)
            return
        }

        addOnUpdateListener(onUpdateListener)
        addOnProgressUpdateListener(onProgressUpdateListener)
        updateStatus(Status.PREPARING)
        downloadFile(
            context,
            message,
            object : OnVoiceFileDownloadListener {
                override fun onVoiceFileDownloaded(
                    voiceFile: File?,
                    e: SendbirdException?
                ) {
                    Logger.i(">> VoicePlayer::onVoiceFileDownloaded, status=$status")
                    if (e != null || status != Status.PREPARING || voiceFile == null) {
                        stop()
                        return
                    }
                    play(context, voiceFile, duration, onUpdateListener, onProgressUpdateListener)
                }
            }
        )
    }

    @UiThread
    @Synchronized
    fun play(
        context: Context,
        voiceFile: File,
        duration: Int,
        onUpdateListener: OnUpdateListener,
        onProgressUpdateListener: OnProgressUpdateListener
    ) {
        Logger.i("VoicePlayer::play(), status=%s", status)
        if (status == Status.PLAYING) return

        addOnUpdateListener(onUpdateListener)
        addOnProgressUpdateListener(onProgressUpdateListener)
        prepare(context, voiceFile.absolutePath, duration)
        player.start()
        updateStatus(Status.PLAYING)
        startProgressExecutor()
    }

    private fun prepare(context: Context, filePath: String, duration: Int) {
        Logger.i("VoicePlayer::prepare()")
        if (status == Status.PAUSED) return
        updateStatus(Status.PREPARING)
        this.duration = duration
        player.run {
            try {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, Uri.parse(filePath))
                setOnErrorListener { _, _, _ ->
                    this@VoicePlayer.stop()
                    true
                }
                setOnCompletionListener {
                    this@VoicePlayer.stop()
                }
                prepare()
            } catch (e: Throwable) {
                Logger.w(e)
                this@VoicePlayer.stop()
            }
        }
    }

    @UiThread
    @Synchronized
    fun pause() {
        if (status == Status.STOPPED || status == Status.PAUSED) return
        Logger.i("VoicePlayer::pause(), seekTo=${getSeekTo()}")

        progressExecutor.cancelAllJobs(true)
        updateStatus(Status.PAUSED)
        updateProgress(getSeekTo())
        player.pause()
    }

    @UiThread
    @Synchronized
    fun stop() {
        if (status == Status.STOPPED) return
        Logger.i("VoicePlayer::stop()")

        progressExecutor.cancelAllJobs(true)
        updateStatus(Status.STOPPED)
        updateProgress(0)
        player.reset()
    }

    @UiThread
    @Synchronized
    private fun updateStatus(status: Status) {
        if (this.status == status) return
        Logger.i("VoicePlayer::updateProgress(), status : $status")

        this.status = status
        onUpdateListenerSet.forEach {
            it.onUpdated(key, status)
        }
    }

    @UiThread
    @Synchronized
    private fun updateProgress(currentPosition: Int) {
        Logger.i("VoicePlayer::updateProgress(), currentPosition : $currentPosition")

        onProgressUpdateListenerSet.forEach {
            it.onProgressUpdated(
                key,
                status,
                currentPosition,
                duration
            )
        }
    }

    @Synchronized
    fun getSeekTo(): Int = player.currentPosition

    private fun startProgressExecutor() {
        Logger.i("VoicePlayer::startProgressExecutor()")
        progressExecutor.cancelAllJobs(true)
        progressExecutor.scheduleAtFixedRate({
            runOnUiThread {
                try {
                    Logger.i("VoicePlayer >> onProgress, current pos : ${getSeekTo()}, status : $status")
                    if (status == Status.PLAYING) {
                        updateProgress(getSeekTo())
                    }
                } catch (ignore: Throwable) {
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS)
    }

    @Synchronized
    fun dispose() {
        Logger.i("VoicePlayer::dispose()")
        player.release()
        progressExecutor.shutdownNow()
        onUpdateListenerSet.clear()
        onProgressUpdateListenerSet.clear()
        status = Status.STOPPED
    }

    @AnyThread
    @Synchronized
    fun addOnUpdateListener(onUpdateListener: OnUpdateListener) {
        onUpdateListenerSet.add(onUpdateListener)
    }

    @AnyThread
    @Synchronized
    fun removeOnUpdateListener(onUpdateListener: OnUpdateListener) {
        onUpdateListenerSet.remove(onUpdateListener)
    }

    @AnyThread
    @Synchronized
    fun addOnProgressUpdateListener(onProgressUpdateListener: OnProgressUpdateListener) {
        onProgressUpdateListenerSet.add(onProgressUpdateListener)
    }

    @AnyThread
    @Synchronized
    fun removeOnProgressListener(onProgressUpdateListener: OnProgressUpdateListener) {
        onProgressUpdateListenerSet.remove(onProgressUpdateListener)
    }

    private fun getData(context: Context, fileMessage: FileMessage): File? {
        val voiceFile = FileUtils.getVoiceFile(context, fileMessage)
        if (voiceFile.exists()) {
            if (voiceFile.length().toInt() == fileMessage.size) {
                Logger.dev("__ return exist voice file")
                return voiceFile
            }
        }
        return null
    }

    private fun downloadFile(
        context: Context,
        fileMessage: FileMessage,
        listener: OnVoiceFileDownloadListener? = null
    ) {
        FileDownloader.downloadFile(
            context,
            fileMessage,
            object : OnResultHandler<File> {
                override fun onResult(file: File) {
                    listener?.onVoiceFileDownloaded(file, null)
                }

                override fun onError(e: SendbirdException?) {
                    listener?.onVoiceFileDownloaded(null, e)
                }
            }
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VoicePlayer) return false

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}
