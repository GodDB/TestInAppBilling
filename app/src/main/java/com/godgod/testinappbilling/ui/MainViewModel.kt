package com.godgod.testinappbilling.ui

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.godgod.testinappbilling.model.BillingState
import com.godgod.testinappbilling.model.InAppItem
import com.godgod.testinappbilling.model.SubItem
import com.godgod.testinappbilling.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val billingRepository: BillingRepository) : ViewModel() {

    val billingState: LiveData<BillingState> = billingRepository.fetchBillingState()
        .asLiveData(viewModelScope.coroutineContext)

    fun purchaseItem(item: InAppItem, getActivity: () -> Activity) {
        billingRepository.purchaseItem(item, getActivity)
    }
    fun purchaseItem(item: SubItem, getActivity: () -> Activity) {
        billingRepository.purchaseItem(item, getActivity)
    }
    suspend fun refreshPurchasedItems() {
        billingRepository.refreshPurchasedItems()
    }
}