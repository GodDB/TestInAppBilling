package com.godgod.testinappbilling.datasource

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.godgod.testinappbilling.di.MainCoroutineScope
import com.godgod.testinappbilling.model.BillingState
import com.godgod.testinappbilling.model.InAppItem
import com.godgod.testinappbilling.model.SkuDetailsList
import com.godgod.testinappbilling.model.SubItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BillingDataSourceImpl @Inject constructor(
    @ApplicationContext applicationContext: Context,
    @MainCoroutineScope private val defaultScope: CoroutineScope
) : BillingDataSource {
    companion object {
        /** 아이템 조회를 위한 key값 */
        private val INAPP_ITEM_ID_LIST = listOf(InAppItem.POINT_600.itemKey, InAppItem.POINT_10000.itemKey)
        private val SUB_ITEM_ID_LIST = listOf(SubItem.ONE_MONTH_EMAIL.itemKey, SubItem.ONE_MONTH_GOOGLE.itemKey, SubItem.ONE_MONTH_KAKAO.itemKey)
    }
    private val billingStateFlow: MutableSharedFlow<BillingState> = MutableSharedFlow(extraBufferCapacity = 10)
    override fun fetchBillingState(): Flow<BillingState> = billingStateFlow.asSharedFlow()
    /** 아이템 조회의 결과물 */
    private val inAppItemList: SkuDetailsList = SkuDetailsList()
    private val subscriptionItemList: SkuDetailsList = SkuDetailsList()
    /** billing api setup listener */
    private val billingStateListener = object : BillingClientStateListener {
        override fun onBillingServiceDisconnected() {
            handleBillingServiceDisconnected()
        }
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            handleBillingSetupFinished(billingResult)
            // 결제 Pending된 아이템들이 Purchase 상태로 변경되었을 때, 구매 처리를 한다.
            defaultScope.launch {
                handlePurchases(queryInAppPurchaseItems().purchasesList)
            }
        }
    }
    /** billing ui를 통해 구매 처리 뜨면 반응하는 리스너 */
    private val purchaseUpdateListener = object : PurchasesUpdatedListener {
        override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
            handlePurchasesUpdated(billingResult, purchases)
        }
    }
    private val billingClient = BillingClient.newBuilder(applicationContext)
        .enablePendingPurchases()
        .setListener(purchaseUpdateListener)
        .build()
        .apply { startConnection(billingStateListener) }
    /** 사용자가 구매한 아이템을 조회한다. debugging 용도*/
    override suspend fun refreshPurchasedItems() {
        Log.d("godgod", "인앱 결제 리스트 ${queryInAppPurchaseItems().purchasesList}")
        Log.d("godgod", "구독 결제 리스트 ${querySubscriptionPurchaseItems().purchasesList}")
    }
    /** 인앱 아이템 구매를 위한 billing ui를 띄운다. */
    override fun purchaseItem(item: InAppItem, getActivity: () -> Activity) {
        val skuDetails = inAppItemList.findSkuDetailsByItemKey(itemKey = item.itemKey) ?: return
        showBillingUI(skuDetails, getActivity)
    }
    /** 구독 아이템 구매를 위한 billing ui를 띄운다. */
    override fun purchaseItem(item: SubItem, getActivity: () -> Activity) {
        if (!billingClient.isSupportSubscription()) {
            billingStateFlow.tryEmit(BillingState.FeatureNotSupported)
            return
        }
        val skuDetails = subscriptionItemList.findSkuDetailsByItemKey(itemKey = item.itemKey) ?: return
        showBillingUI(skuDetails, getActivity)
    }
    /** billing ui를 띄운다. */
    private fun showBillingUI(skuDetails: SkuDetails, getActivity: () -> Activity) {
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        billingClient.launchBillingFlow(getActivity(), flowParams)
    }
    /**
     *  purchasedUpdate listener의 이벤트를 처리한다.
     *
     *  @see BillingClient.BillingResponseCode
     *  BILLING_UNAVAILABLE - Billing API v4 버전이 지원되지 않음
     *  DEVELOPER_ERROR - 올바르지 않은 Parameter를 던짐 [SkuDetails]
     *  ERROR - api 에러
     *  FEATURE_NOT_SUPPORTED - 현재 기능(인앱결제 / 구독결제)는 현재 playstore 버전에서 지원하지 않음 (play store 버전 업데이트 필요)
     *  ITEM_ALREADY_OWNED - 비 소비성 아이템에 대해서 이미 갖고 있는 아이템임
     *  ITEM_NOT_OWNED - 소유 항목이 아니다 ... 정확하게 무슨말이지?
     *  ITEM_UNAVAILABLE - 결제가 불가한 아이템
     *  SERVICE_DISCONNECTED - play store 연결 끊어짐
     *  SERVICE_TIMEOUT - google play response time Out
     *  SERVICE_UNAVAILABLE - 네트워크 연결 안됨
     *  USER_CANCELLED - 유저가 결제 종료함
     */
    private fun handlePurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases?.let { handlePurchases(purchases) }
        } else {
            billingStateFlow.tryEmit(BillingState.byBillingResponseCode(billingResult.responseCode))
        }
    }
    /** billing state listener의 billingSetupFinished 이벤트를 처리한다 */
    private fun handleBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) return
        defaultScope.launch {
            // 인앱 결제 아이템 리스트 조회
            val inAppItems = queryInAppItems()
            val subItems = querySubscriptionItems()
            if (inAppItems.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                inAppItemList.setAll(inAppItems.skuDetailsList ?: emptyList())
            }
            if (subItems.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                subscriptionItemList.setAll(subItems.skuDetailsList ?: emptyList())
            }
        }
    }
    /** billing state listener의 billingServiceDisconnected 이벤트를 처리한다 */
    private fun handleBillingServiceDisconnected() {
        billingClient.startConnection(billingStateListener)
    }
    /** 사용자의 playstore 계정으로 구매한 인앱 아이템을 조회한다 */
    private suspend fun queryInAppPurchaseItems(): PurchasesResult {
        return billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP)
    }
    /** 사용자의 playstore 계정으로 구매한 구독 아이템을 조회한다. */
    private suspend fun querySubscriptionPurchaseItems(): PurchasesResult {
        return billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS)
    }
    /** 사용자의 playstore 계정으로 구매 가능한 인앱 아이템을 조회한다. */
    private suspend fun queryInAppItems(): SkuDetailsResult {
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(INAPP_ITEM_ID_LIST)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        return withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params)
        }
    }
    /** 사용자의 playstore 계정으로 구매 가능한 구독 아이템을 조회한다. */
    private suspend fun querySubscriptionItems(): SkuDetailsResult {
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(SUB_ITEM_ID_LIST)
            .setType(BillingClient.SkuType.SUBS)
            .build()
        return withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params)
        }
    }
    /** 사용자가 google store ui를 통해 한 구매들을 확인하여 구매 상태를 내려준다. */
    private fun handlePurchases(purchases: List<Purchase>) {
        defaultScope.launch {
            for (purchase in purchases) {
                if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
                    billingStateFlow.tryEmit(BillingState.PendingPurchase)
                } else {
                    billingStateFlow.tryEmit(BillingState.PurchaseNotApproved(purchase))
                }
            }
        }
    }
    /** 서버에서 검증을 마친 아이템에 대해서 결제 처리를 한다. */
    override suspend fun approvePurchaseItem(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            billingStateFlow.tryEmit(BillingState.PendingPurchase)
            return
        }
        if (purchase.isAutoRenewing) {
            approveSubPurchase(purchase)
        } else {
            approveInAppPurchase(purchase)
        }
    }
    /** inapp 결제의 아이템에 대해서 구매 승낙 처리를 한다. */
    private suspend fun approveInAppPurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val result = withContext(Dispatchers.IO) {
            billingClient.consumePurchase(consumeParams)
        }
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            billingStateFlow.tryEmit(BillingState.SuccessPurchase)
        }
        refreshPurchasedItems()
    }
    /** subscription 결제의 아이템에 대해서 구매 승낙 처리를 한다. */
    private suspend fun approveSubPurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val result = withContext(Dispatchers.IO) {
            billingClient.acknowledgePurchase(acknowledgePurchaseParams)
        }
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            billingStateFlow.tryEmit(BillingState.SuccessPurchase)
        }
        refreshPurchasedItems()
    }
}

fun BillingClient.isSupportSubscription(): Boolean =
    this.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).responseCode == BillingClient.BillingResponseCode.OK