package com.godgod.testinappbilling.di

import com.godgod.testinappbilling.repository.BillingRepository
import com.godgod.testinappbilling.repository.BillingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindBillingRepository(billingRepositoryImpl: BillingRepositoryImpl) : BillingRepository
}