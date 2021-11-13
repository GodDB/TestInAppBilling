package com.godgod.testinappbilling.datasource

import android.app.Activity
import com.android.billingclient.api.Purchase
import com.godgod.testinappbilling.model.BillingState
import com.godgod.testinappbilling.model.InAppItem
import com.godgod.testinappbilling.model.PurchaseVerifyState
import com.godgod.testinappbilling.model.SubItem
import kotlinx.coroutines.flow.Flow

interface BillingDataSource {
    fun fetchBillingState(): Flow<BillingState>
    fun purchaseItem(item: InAppItem, getActivity: () -> Activity)
    fun purchaseItem(item: SubItem, getActivity: () -> Activity)
    suspend fun approvePurchaseItem(state: PurchaseVerifyState)
    suspend fun refreshPurchasedItems()
}