package com.godgod.testinappbilling.model

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase

sealed class BillingState {
    object BillingUnavailable : BillingState() // Billing API v4 버전이 지원되지 않음
    object DeveloperError : BillingState() // 올바르지 않은 Parameter를 던짐 [SkuDetails]
    object Error : BillingState()  // api 에러
    object FeatureNotSupported : BillingState() // 인앱결제 / 구독결제는 현재 playstore 버전에서 지원하지 않음 (play store 버전 업데이트 필요)
    object ItemAlreadyOwned : BillingState()  // 비 소비성 아이템에 대해서 이미 갖고 있는 아이템임
    object ItemNotOwned : BillingState() // 소유 항목이 아니다 ... 정확하게 무슨말이지?
    object ItemUnavailable : BillingState() // 결제가 불가한 아이템
    object ServiceDisconnected : BillingState() // play store 연결 끊어짐
    object ServiceTimeOut : BillingState() // google play response time Out
    object ServiceUnAvailable : BillingState() // 네트워크 연결 안됨
    object UserCancelled : BillingState() // 유저가 결제 종료함

    // 이 4개에 대해선 converter를 거치지 않고 사용한다.
    data class PurchaseProcessing(val purchase : Purchase) : BillingState() // 유저가 구매 했으나 구매 승인이 안이뤄짐 - 구매 처리 중
    object SuccessPurchase : BillingState() // 유저가 구매 후, 승인까지 완료
    object PendingPurchase : BillingState() // 결제 유보
    object UnVerifiedPurchase : BillingState() // 승인되지 않은 구매 - 서버에서 검증 실패

    companion object {
        fun byBillingResponseCode(@BillingClient.BillingResponseCode responseCode : Int) : BillingState {
            return when(responseCode) {
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> BillingUnavailable
                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> DeveloperError
                BillingClient.BillingResponseCode.ERROR -> Error
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> FeatureNotSupported
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> ItemAlreadyOwned
                BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> ItemNotOwned
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> ItemUnavailable
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> ServiceDisconnected
                BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> ServiceTimeOut
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> ServiceUnAvailable
                BillingClient.BillingResponseCode.USER_CANCELED -> UserCancelled
                else -> throw Exception("not handle response code")
            }
        }
    }
}