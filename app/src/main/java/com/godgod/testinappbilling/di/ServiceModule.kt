package com.godgod.testinappbilling.di

import com.godgod.testinappbilling.service.BillingVerifyService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    fun provideBillingVerifyService(retrofit: Retrofit): BillingVerifyService =
        retrofit.create(BillingVerifyService::class.java)
}