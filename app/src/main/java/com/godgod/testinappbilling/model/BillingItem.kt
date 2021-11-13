package com.godgod.testinappbilling.model

enum class SubItem(val itemKey: String) {
    ONE_MONTH("subscription_1_month"),
    THREE_MONTH("subscription_3_month")
}
enum class InAppItem(val itemKey: String) {
    POINT_1000("point_1000"),
    POINT_10000("point_10000")
}