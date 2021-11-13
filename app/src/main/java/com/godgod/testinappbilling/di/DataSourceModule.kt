package com.godgod.testinappbilling.di

import com.godgod.testinappbilling.datasource.BillingDataSource
import com.godgod.testinappbilling.datasource.BillingDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    abstract fun bindBillingDataSource(billingDataSourceImpl: BillingDataSourceImpl) : BillingDataSource
}