package com.godgod.testinappbilling.model

enum class SubItem(val itemKey: String) {
    ONE_MONTH_KAKAO("sub_1_month_kakao"),
    ONE_MONTH_GOOGLE("sub_1_month_google"),
    ONE_MONTH_EMAIL("sub_1_month_email")
}
enum class InAppItem(val itemKey: String) {
    POINT_600("point_600"),
    POINT_10000("point_10000")
}