package com.godgod.testinappbilling.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import com.godgod.testinappbilling.R
import com.godgod.testinappbilling.model.BillingState
import com.godgod.testinappbilling.model.InAppItem
import com.godgod.testinappbilling.model.SubItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_1_month_sub).setOnClickListener {
            viewModel.purchaseItem(SubItem.ONE_MONTH) { this }
        }

        findViewById<Button>(R.id.btn_3_month_sub).setOnClickListener {
            viewModel.purchaseItem(SubItem.THREE_MONTH) { this }
        }

        findViewById<Button>(R.id.btn_point_1000).setOnClickListener {
            viewModel.purchaseItem(InAppItem.POINT_1000) { this }
        }

        findViewById<Button>(R.id.btn_point_10000).setOnClickListener {
            viewModel.purchaseItem(InAppItem.POINT_10000) { this }
        }

        observeLivedata()
    }

    private fun observeLivedata() {
        viewModel.billingState.observe(this) {
            val msg = when(it) {
                is BillingState.PendingPurchase -> "결제 보류"
                is BillingState.BillingUnavailable -> "지원 불가"
                is BillingState.ItemNotOwned -> "소유하지 않은 아이템"
                is BillingState.PurchaseProcessing -> "구매 진행 중"
                is BillingState.UserCancelled -> "취소"
                is BillingState.ServiceUnAvailable -> "서비스 불가"
                is BillingState.ServiceTimeOut -> "타임 아웃"
                is BillingState.ServiceDisconnected -> "연결 끊김"
                is BillingState.ItemUnavailable -> "아이템 구매 불가"
                is BillingState.ItemAlreadyOwned -> "아이템 이미 소유중"
                is BillingState.FeatureNotSupported -> "인앱/구독 결제 미지원 버전"
                is BillingState.DeveloperError -> "잘못된 input"
                is BillingState.Error -> "에러 발생"
                is BillingState.SuccessPurchase -> "구매 성공"
                is BillingState.UnVerifiedPurchase -> "구매 처리가 되지 않음"
            }
            showMessage(msg)
        }
    }
    var toast : Toast? = null
    private fun showMessage(msg : String) {
        if(msg.isEmpty()) return
        toast?.cancel()
        toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT).apply { show() }
    }
}