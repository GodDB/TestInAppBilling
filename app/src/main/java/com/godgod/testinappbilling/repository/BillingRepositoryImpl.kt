package com.godgod.testinappbilling.repository

import android.app.Activity
import com.android.billingclient.api.Purchase
import com.godgod.testinappbilling.datasource.BillingDataSource
import com.godgod.testinappbilling.model.BillingState
import com.godgod.testinappbilling.model.InAppItem
import com.godgod.testinappbilling.model.SubItem
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class BillingRepositoryImpl @Inject constructor(
    private val billingDataSource: BillingDataSource
) : BillingRepository {
    @FlowPreview
    override fun fetchBillingState(): Flow<BillingState> = billingDataSource.fetchBillingState()
        .onEach {
            if(it is BillingState.PurchaseNotApproved) {
                approvePurchase(it.purchase)
            }
        }.filterNot { it is BillingState.PurchaseNotApproved } // 구매 처리중 이므로 내리지 않는다.

    override fun purchaseItem(item: InAppItem, getActivity: () -> Activity) {
        billingDataSource.purchaseItem(item, getActivity)
    }
    override fun purchaseItem(item: SubItem, getActivity: () -> Activity) {
        billingDataSource.purchaseItem(item, getActivity)
    }
    override suspend fun refreshPurchasedItems() {
        billingDataSource.refreshPurchasedItems()
    }
    private suspend fun approvePurchase(purchase : Purchase) {
        //todo api call
        // https://developer.android.com/google/play/billing/security?hl=ko#verify
        billingDataSource.approvePurchaseItem(purchase)
    }
}