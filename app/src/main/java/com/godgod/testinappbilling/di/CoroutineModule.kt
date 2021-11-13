package com.godgod.testinappbilling.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier


@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {
    @IOCoroutineScope
    @Provides
    fun provideIoCoroutineScope() : CoroutineScope = CoroutineScope(Dispatchers.IO)
    @MainCoroutineScope
    @Provides
    fun provideMainCoroutineScope() : CoroutineScope = CoroutineScope(Dispatchers.Main.immediate)
}
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IOCoroutineScope
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainCoroutineScope