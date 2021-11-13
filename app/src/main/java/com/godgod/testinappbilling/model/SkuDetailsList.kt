package com.godgod.testinappbilling.model

import com.android.billingclient.api.SkuDetails

class SkuDetailsList : MutableList<SkuDetails> by mutableListOf() {
    fun setAll(elements : Collection<SkuDetails>) {
        this.clear()
        this.addAll(elements)
    }
    fun findSkuDetailsByItemKey(itemKey : String) : SkuDetails? {
        return this.find { it.sku == itemKey }
    }
}