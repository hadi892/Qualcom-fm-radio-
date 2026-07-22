package com.example.fmservice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.fmjni.FmNativeBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FmRadioService : Service() {

    private val binder = FmBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var rdsJob: Job? = null

    private lateinit var audioRouter: FmAudioRouter
    private var mediaSession: MediaSession? = null

    private val _currentFreqKhz = MutableStateFlow(100700)
    val currentFreqKhz: StateFlow<Int> = _currentFreqKhz.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _rdsData = MutableStateFlow(RdsData("QCOM-FM", "100.7 MHz Live FM", 10, -50, true))
    val rdsData: StateFlow<RdsData> = _rdsData.asStateFlow()

    inner class FmBinder : Binder() {
        fun getService(): FmRadioService = this@FmRadioService
    }

    override fun onCreate() {
        super.onCreate()
        audioRouter = FmAudioRouter(this)
        createNotificationChannel()
        initMediaSession()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_TOGGLE_PLAY -> if (_isPlaying.value) pause() else play()
            ACTION_TUNE -> {
                val freq = intent.getIntExtra(EXTRA_FREQ_KHZ, 100700)
                tuneTo(freq)
            }
            ACTION_SEEK_UP -> seek(true)
            ACTION_SEEK_DOWN -> seek(false)
        }
        return START_STICKY
    }

    fun play() {
        if (audioRouter.requestAudioFocus()) {
            _isPlaying.value = true
            FmNativeBridge.enable(0, 1, 0)
            FmNativeBridge.setTune(_currentFreqKhz.value)
            startForegroundServiceNotification()
            startRdsPolling()
            updateMediaSessionState()
        }
    }

    fun pause() {
        _isPlaying.value = false
        FmNativeBridge.disable()
        audioRouter.abandonAudioFocus()
        stopRdsPolling()
        updateMediaSessionState()
        stopForeground(STOP_FOREGROUND_DETACH)
    }

    fun tuneTo(freqKhz: Int) {
        _currentFreqKhz.value = freqKhz
        FmNativeBridge.setTune(freqKhz)
        _rdsData.value = FmNativeBridge.getRdsData(freqKhz)
        if (_isPlaying.value) {
            startForegroundServiceNotification()
            updateMediaSessionState()
        }
    }

    fun seek(scanUp: Boolean) {
        val newFreq = FmNativeBridge.seek(scanUp, _currentFreqKhz.value)
        tuneTo(newFreq)
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun toggleSpeaker(speakerOn: Boolean) {
        audioRouter.setSpeakerphoneOn(speakerOn)
    }

    private fun startRdsPolling() {
        stopRdsPolling()
        rdsJob = serviceScope.launch {
            while (_isPlaying.value) {
                _rdsData.value = FmNativeBridge.getRdsData(_currentFreqKhz.value)
                delay(2000)
            }
        }
    }

    private fun stopRdsPolling() {
        rdsJob?.cancel()
        rdsJob = null
    }

    private fun initMediaSession() {
        mediaSession = MediaSession(this, "FmRadioServiceSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() { play() }
                override fun onPause() { pause() }
                override fun onSkipToNext() { seek(true) }
                override fun onSkipToPrevious() { seek(false) }
            })
            isActive = true
        }
    }

    private fun updateMediaSessionState() {
        val state = if (_isPlaying.value) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
        val playbackState = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or
                        PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_SKIP_TO_NEXT or
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS
            )
            .setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f)
            .build()
        mediaSession?.setPlaybackState(playbackState)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FM Radio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active FM Radio playback controls"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceNotification() {
        val mhzStr = String.format("%.1f MHz", _currentFreqKhz.value / 1000.0f)
        val stationTitle = _rdsData.value.psName.ifEmpty { "FM $mhzStr" }

        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val togglePlayIntent = Intent(this, FmRadioService::class.java).apply { action = ACTION_TOGGLE_PLAY }
        val pendingTogglePlay = PendingIntent.getService(this, 1, togglePlayIntent, PendingIntent.FLAG_IMMUTABLE)

        val seekNextIntent = Intent(this, FmRadioService::class.java).apply { action = ACTION_SEEK_UP }
        val pendingSeekNext = PendingIntent.getService(this, 2, seekNextIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Qualcomm FM Radio • $stationTitle")
            .setContentText(_rdsData.value.radioText)
            .setSubText(mhzStr)
            .setContentIntent(pendingOpenApp)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                if (_isPlaying.value) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (_isPlaying.value) "Pause" else "Play",
                pendingTogglePlay
            )
            .addAction(android.R.drawable.ic_media_next, "Seek Next", pendingSeekNext)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        stopRdsPolling()
        mediaSession?.release()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "fm_radio_playback_channel"
        const val NOTIFICATION_ID = 2026

        const val ACTION_PLAY = "com.example.fm.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.fm.ACTION_PAUSE"
        const val ACTION_TOGGLE_PLAY = "com.example.fm.ACTION_TOGGLE_PLAY"
        const val ACTION_TUNE = "com.example.fm.ACTION_TUNE"
        const val ACTION_SEEK_UP = "com.example.fm.ACTION_SEEK_UP"
        const val ACTION_SEEK_DOWN = "com.example.fm.ACTION_SEEK_DOWN"

        const val EXTRA_FREQ_KHZ = "extra_freq_khz"
    }
}
