package com.example.musicplayer.di

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped


// All dependencies live as long as their services
@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    // Save meta information about the player
    @Provides
    @ServiceScoped  // Use same instance of audio attributes when within the same service instance
    fun provideAudioAttributes() =
        AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

    @Provides
    @ServiceScoped
    fun provideExoPlayer(@ApplicationContext context: Context, audioAttributes: AudioAttributes) =
        ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)  // Pause player if user plugs in headphones
        }

    @Provides
    @ServiceScoped
    fun provideDataSourceFactory(@ApplicationContext context: Context) =
        DefaultDataSource.Factory(context)  // May need user agent `Util.getUserAgent(context, "Music Player App")`
}
