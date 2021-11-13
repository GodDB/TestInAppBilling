package com.godgod.testinappbilling.repository

import android.app.Activity
import com.godgod.testinappbilling.model.BillingState
import com.godgod.testinappbilling.model.InAppItem
import com.godgod.testinappbilling.model.SubItem
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    fun fetchBillingState(): Flow<BillingState>
    fun purchaseItem(item: InAppItem, getActivity: () -> Activity)
    fun purchaseItem(item: SubItem, getActivity: () -> Activity)
    suspend fun refreshPurchasedItems()
}