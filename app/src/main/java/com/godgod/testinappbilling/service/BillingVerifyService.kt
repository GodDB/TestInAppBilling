package com.godgod.testinappbilling.service

import com.android.billingclient.api.BillingClient
import com.godgod.testinappbilling.model.PurchaseVerifyState
import com.google.gson.JsonObject
import retrofit2.http.POST
import retrofit2.http.Query

interface BillingVerifyService {

    @POST("verifyPurchases")
    suspend fun verifyPurchase(
        @Query("itemType") @BillingClient.SkuType itemType: String,
        @Query("packageName") packageName: String,
        @Query("productId") productId: String,
        @Query("purchaseToken") purchaseToken: String
    ) : PurchaseVerifyState
}