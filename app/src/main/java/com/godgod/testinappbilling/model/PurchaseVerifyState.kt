package com.godgod.testinappbilling.model

sealed class PurchaseVerifyState {

    // 네트워크 에러
    data class NetworkError(val throwable: Throwable) : PurchaseVerifyState()

    // 성공
    data class Success(val purchaseToken : String = "", val itemType : String) : PurchaseVerifyState()

    // 검증 과정에서의 에러
    data class VerifyError(val errorCode : Int, val errorMessage : String) : PurchaseVerifyState()

}