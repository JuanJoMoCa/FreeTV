package com.example.freetv.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource

object VideoPlayerManager {
    var exoPlayer: ExoPlayer? = null
        private set

    private var onErrorCallback: (() -> Unit)? = null
    private var currentStreamUrl: String? = null

    var isAudioOnlyActive = false
        private set

    @OptIn(UnstableApi::class)
    fun getPlayer(context: Context, url: String, onError: () -> Unit): ExoPlayer {
        this.onErrorCallback = onError

        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context.applicationContext).build().apply {
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        onErrorCallback?.invoke()
                    }
                })
            }
        }

        if (currentStreamUrl != url) {
            currentStreamUrl = url

            val dataSourceFactory = DefaultHttpDataSource.Factory()
            val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(url))

            exoPlayer?.apply {
                setMediaSource(mediaSource)
                prepare()
                playWhenReady = true

                setAudioOnly(isAudioOnlyActive)
            }
        }

        return exoPlayer!!
    }

    fun setAudioOnly(isAudioOnly: Boolean) {
        isAudioOnlyActive = isAudioOnly
        exoPlayer?.let { player ->
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, isAudioOnly)
                .build()
        }
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
        onErrorCallback = null
        currentStreamUrl = null
        isAudioOnlyActive = false
    }
}