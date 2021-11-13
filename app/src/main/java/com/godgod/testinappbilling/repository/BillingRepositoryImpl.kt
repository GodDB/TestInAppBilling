package com.godgod.testinappbilling.repository

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.godgod.testinappbilling.datasource.BillingDataSource
import com.godgod.testinappbilling.model.BillingState
import com.godgod.testinappbilling.model.InAppItem
import com.godgod.testinappbilling.model.PurchaseVerifyState
import com.godgod.testinappbilling.model.SubItem
import com.godgod.testinappbilling.service.BillingVerifyService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class BillingRepositoryImpl @Inject constructor(
    private val billingDataSource: BillingDataSource,
    private val billingVerifyService: BillingVerifyService
) : BillingRepository {

    @FlowPreview
    override fun fetchBillingState(): Flow<BillingState> = billingDataSource.fetchBillingState()
        .onEach {
            if (it is BillingState.PurchaseProcessing) {
                verifyPurchase(it.purchase)
            }
        }.filterNot { it is BillingState.PurchaseProcessing } // 구매 처리중 이므로 내리지 않는다.


    override fun purchaseItem(item: InAppItem, getActivity: () -> Activity) {
        billingDataSource.purchaseItem(item, getActivity)
    }

    override fun purchaseItem(item: SubItem, getActivity: () -> Activity) {
        billingDataSource.purchaseItem(item, getActivity)
    }

    override suspend fun refreshPurchasedItems() {
        billingDataSource.refreshPurchasedItems()
    }

    private suspend fun verifyPurchase(purchase: Purchase) {
        // https://developer.android.com/google/play/billing/security?hl=ko#verify
        flow {
            emit(
                billingVerifyService.verifyPurchase(
                    itemType = if (purchase.isAutoRenewing) BillingClient.SkuType.SUBS else BillingClient.SkuType.INAPP,
                    packageName = purchase.packageName,
                    productId = purchase.skus.first(), // 아이템은 한번에 하나만 살 수 있음
                    purchaseToken = purchase.purchaseToken
                )
            )
        }.retry(5) { it is Exception } // 서버 에러시 최대 5번 호출
            .catch { emit(PurchaseVerifyState.NetworkError(it)) }
            .map {
                if (it is PurchaseVerifyState.Success) it.copy(
                    purchaseToken = purchase.purchaseToken,
                    itemType = if (purchase.isAutoRenewing) BillingClient.SkuType.SUBS else BillingClient.SkuType.INAPP
                ) else {
                    it
                }
            }
            .collect {
                billingDataSource.approvePurchaseItem(it)
            }
    }


}

