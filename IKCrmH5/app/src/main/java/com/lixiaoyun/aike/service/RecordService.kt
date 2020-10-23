package com.lixiaoyun.aike.service

import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.IBinder
import com.lixiaoyun.aike.constant.AppConfig
import com.lixiaoyun.aike.entity.RecordEvent
import com.lixiaoyun.aike.utils.DateUtils
import com.lixiaoyun.aike.utils.getInternalStorageFile
import com.lixiaoyun.aike.utils.toast
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.EventBus
import java.io.IOException

/**
 * @data on 2019/5/14
 * 录音service
 */
class RecordService : Service() {

    private var mRecorder: MediaRecorder? = null
    private var mAudioPath: String? = null
    private var mAudioName: String? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startRecording()
        return START_STICKY
    }

    override fun onDestroy() {
        if (mRecorder != null) {
            stopRecording()
        }
        super.onDestroy()
    }

    private fun startRecording() {
        mAudioPath = getAudioFilePath()
        mRecorder = MediaRecorder()
        mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mRecorder?.setAudioChannels(1)
        mRecorder?.setAudioSamplingRate(44100)
        mRecorder?.setAudioEncodingBitRate(48000)
        mRecorder?.setOutputFile(mAudioPath)
        try {
            mRecorder?.prepare()
            mRecorder?.start()
            Logger.e("开始录音")
        } catch (e: IOException) {
            Logger.e("record prepare error: ${e.message}")
        }
    }

    private fun getAudioFilePath(): String? {
        val audioFolder = AppConfig.AIKE_AUDIO_PATH.getInternalStorageFile()
        mAudioName = "${DateUtils.instance.getNowString(DateUtils.FORMAT_UNDERLINED)}${AppConfig.AIKE_AUDIO_SUFFIX}.mp3"
        return "${audioFolder.absolutePath}/$mAudioName"
    }

    private fun stopRecording() {
        EventBus.getDefault().post(RecordEvent(mAudioPath ?: ""))
        mRecorder?.stop()
        mRecorder?.release()
        Logger.e("录音完成")
        mRecorder = null
    }
}