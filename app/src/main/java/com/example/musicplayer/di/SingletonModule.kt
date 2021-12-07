package com.example.musicplayer.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.R
import com.example.musicplayer.exoplayer.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


// All dependencies live as long as the application does
@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {

    @Provides
    @Singleton  // Ensures only one Glide instance is created (a singleton)
    fun provideGlideInstance(@ApplicationContext context: Context) =
        Glide.with(context).setDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.drawable.ic_image)  // Default placeholder image
                .error(R.drawable.ic_image)  // If something goes wrong, display image
                .diskCacheStrategy(DiskCacheStrategy.DATA)  // Caches images with Glide
        )

    @Provides
    @Singleton
    fun provideMusicServiceConnection(@ApplicationContext context: Context) =
        MusicServiceConnection(context)
}
